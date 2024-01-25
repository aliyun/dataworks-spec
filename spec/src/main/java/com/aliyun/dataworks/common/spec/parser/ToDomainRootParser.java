/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.common.spec.parser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.aliyun.dataworks.common.spec.domain.Spec;
import com.aliyun.dataworks.common.spec.domain.SpecConstants;
import com.aliyun.dataworks.common.spec.domain.SpecEntity;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext.SpecEntityContext;
import com.aliyun.dataworks.common.spec.parser.impl.DataWorksWorkflowSpecParser;
import com.aliyun.dataworks.common.spec.parser.impl.SpecParser;
import com.aliyun.dataworks.common.spec.utils.MapKeyMatchUtils;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.reflections.Reflections;

/**
 * @author yiwei.qyw
 * @date 2023/7/6
 */
@Slf4j
public class ToDomainRootParser {

    Specification<Spec> specification;
    SpecParser<?> specParser;
    SpecParserContext specParserContext;
    Set<Class<? extends SpecRefEntity>> specRefEntityClasses;

    /**
     * parse spec to Domain
     *
     * @param spec spec string
     * @return Specification domain
     */
    public Specification<?> parseToDomain(String spec) {
        if (spec == null) {
            return null;
        }

        // loads Context
        specParserContext = new SpecParserContext();
        // load Parsers
        specParserContext.specInit(spec);

        // create Specification object and spec parser
        initialize();

        // parse specification objects
        preParser();

        // process objects reference relations
        refParser();

        return specification;
    }

    private void initialize() {
        SpecKind kind = LabelEnum.getByLabel(SpecKind.class,
            (String)MapKeyMatchUtils.getIgnoreCaseSingleAndPluralForm(specParserContext.getContextMap(), SpecConstants.SPEC_KEY_KIND));
        SpecVersion version = LabelEnum.getByLabel(SpecVersion.class,
            (String)MapKeyMatchUtils.getIgnoreCaseSingleAndPluralForm(specParserContext.getContextMap(), SpecConstants.SPEC_KEY_VERSION));

        log.info("spec kind: {}, version: {}", kind, version);
        this.specParserContext.setVersion(version);
        SpecException kindInvalidEx = new SpecException(SpecErrorCode.PARSE_ERROR, "Not support spec kind: " + kind);
        Optional.ofNullable(kind).orElseThrow(() -> kindInvalidEx);
        switch (kind) {
            case CYCLE_WORKFLOW:
            case MANUAL_WORKFLOW: {
                this.specification = new Specification<>();
                this.specification.setContext(specParserContext);
                this.specParser = new DataWorksWorkflowSpecParser();
                break;
            }
            default: {
                throw kindInvalidEx;
            }
        }

        Reflections reflections = new Reflections(SpecRefEntity.class.getPackage().getName());
        specRefEntityClasses = reflections.getSubTypesOf(SpecRefEntity.class);
    }

    private void preParser() {
        Map<String, Object> contextMap = specParserContext.getContextMap();
        if (contextMap == null) {
            return;
        }
        // set simple field
        SpecDevUtil.setSimpleField(contextMap, specification);
        // set enum field
        SpecDevUtil.setEnumField(contextMap, specification);
        // set Map field
        SpecDevUtil.setMapField(contextMap, specification);
        // get current jsonObjectMap keys to parse
        parserSpecFields(contextMap, specParserContext);
    }

    private void refParser() {
        List<SpecEntityContext> refEntityList = specParserContext.getRefEntityList();
        Map<String, SpecRefEntity> entityMap = specParserContext.getEntityMap();

        for (SpecEntityContext refElm : refEntityList) {
            replaceObj(refElm, findTargetElm(refElm, entityMap), refElm.getOwnerObject());
        }
    }

    private SpecRefEntity findTargetElm(SpecEntityContext refElm, Map<String, SpecRefEntity> entityMap) {
        if (refElm == null) {
            return null;
        }

        if (!MapUtils.emptyIfNull(entityMap).containsKey(refElm.getEntityKey())) {
            // find ref entities of subclass
            log.debug("find target children entity of: {}", refElm.getEntityName());
            Object id = refElm.getEntityValue();
            return specRefEntityClasses.stream()
                .peek(clz -> log.debug("clz: {}", clz))
                .filter(clz -> clz.getSimpleName().equalsIgnoreCase(refElm.getEntityName()))
                .findFirst().flatMap(clz -> specRefEntityClasses.stream()
                    .filter(clz::isAssignableFrom)
                    .filter(subClz -> !clz.equals(subClz))
                    .peek(subClz -> log.debug("subClz: {}", subClz))
                    .filter(subClz -> MapUtils.emptyIfNull(entityMap).containsKey(subClz.getSimpleName() + "#" + id))
                    .findAny()
                    .map(subClz -> entityMap.get(subClz.getSimpleName() + "#" + id))).orElse(null);
        }
        return entityMap.get(refElm.getEntityKey());
    }

    @SuppressWarnings("unchecked")
    private void parserSpecFields(Map<String, Object> contextMap, SpecParserContext specParserContext) {
        Spec spec;
        if (MapKeyMatchUtils.containsIgnoreCase(contextMap, SpecConstants.SPEC_KEY_SPEC)) {
            spec = specParser.parse((Map<String, Object>)contextMap.get(SpecConstants.SPEC_KEY_SPEC), specParserContext);
        } else {
            spec = specParser.parse(contextMap, specParserContext);
        }
        specification.setSpec(spec);
    }

    @SuppressWarnings("unchecked")
    private void replaceObj(SpecEntityContext refElm, SpecRefEntity targetElm, Object ownerObject) {
        if (targetElm == null) {
            if (refElm.getEntityField() != null && "output".equalsIgnoreCase(refElm.getEntityField().getName())) {
                SpecNodeOutput ar = new SpecNodeOutput();
                ar.setData((String)refElm.getEntityValue());
                ar.setArtifactType(ArtifactType.NODE_OUTPUT);
                SpecDevUtil.setValue(ownerObject, refElm.getEntityField(), ar);
                return;
            }

            if (refElm.getEntityField() != null && "nodeId".equalsIgnoreCase(refElm.getEntityField().getName())) {
                SpecNode nodeId = new SpecNode();
                nodeId.setId((String)refElm.getEntityValue());
                SpecDevUtil.setValue(ownerObject, refElm.getEntityField(), nodeId);
                return;
            }

            throw new SpecException(SpecErrorCode.TARGET_ENTITY_NOT_FOUND,
                "Cannot find target entity.\nspec type:" + refElm.getEntityKey()
                    .replaceAll("#", " id:"));
        }
        if (ownerObject instanceof List) {
            List<SpecEntity> ownerList = (List<SpecEntity>)ownerObject;
            ownerList.add(targetElm);
        } else {
            // set directly
            Field field = refElm.getEntityField();
            SpecDevUtil.setValue(ownerObject, field, targetElm);
        }
        targetElm.setIsRef(true);
    }

}