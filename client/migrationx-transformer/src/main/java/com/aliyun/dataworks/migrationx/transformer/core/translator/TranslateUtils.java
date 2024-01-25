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

package com.aliyun.dataworks.migrationx.transformer.core.translator;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.reflections.Reflections;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * @author 聿剑
 * @date 2022/10/27
 */
@Slf4j
public class TranslateUtils {

    private static final Set<Class<? extends NodePropertyTranslator>> TRANSLATORS;

    static {
        Reflections reflections = new Reflections(NodePropertyTranslator.class.getPackage().getName());
        TRANSLATORS = reflections.getSubTypesOf(NodePropertyTranslator.class);
    }

    public static boolean translate(DwWorkflow workflow, DwNode node,
        List<Class<? extends NodePropertyTranslator>> translatorClasses,
        Supplier<CodeProgramType> nodeTypeSupplier) {
        AtomicBoolean changed = new AtomicBoolean(false);
        SetUtils.emptyIfNull(TRANSLATORS).stream()
            .filter(clz -> ListUtils.emptyIfNull(translatorClasses).stream().anyMatch(clz::equals))
            .map(clz -> {
                try {
                    return clz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    log.warn("{}", e.getMessage());
                }
                return null;
            })
            .filter(Objects::nonNull)
            .filter(translator -> translator.match(workflow, node))
            .forEach(translator -> {
                try {
                    log.debug("translator: {}, rawType: {}, targetType: {}",
                        translator.getClass(), node.getType(), nodeTypeSupplier.get());
                    String rawType = node.getType();
                    node.setType(nodeTypeSupplier.get().name());
                    if (!translator.translate(workflow, node)) {
                        node.setType(rawType);
                    } else {
                        changed.set(true);
                    }
                } catch (org.apache.commons.cli.ParseException e) {
                    throw new BizException(ErrorCode.TRANSLATE_NODE_ERROR).with(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        return changed.get();
    }

    public static boolean translateSparkSubmit(DwNode node, Properties properties) {
        String sparkSubmitAs = properties.getProperty(Constants.CONVERTER_SPARK_SUBMIT_TYPE_AS,
            CodeProgramType.DIDE_SHELL.name());
        log.debug("sparkSubmitAs: {}", sparkSubmitAs);
        SparkSubmitTranslator translator = new SparkSubmitTranslator();
        if (translator.match((DwWorkflow)node.getWorkflowRef(), node)) {
            node.setType(sparkSubmitAs);
            return translator.translate((DwWorkflow)node.getWorkflowRef(), node);
        }
        return false;
    }

    public static boolean translateCommandSql(DwWorkflow workflow, DwNode node, Properties properties) {
        AtomicBoolean changed = new AtomicBoolean(false);
        Reflections reflections = new Reflections(AbstractCommandSqlTranslator.class.getPackage().getName());
        Set<Class<? extends AbstractCommandSqlTranslator>> sqlTranslatorTypes =
            reflections.getSubTypesOf(AbstractCommandSqlTranslator.class);

        properties = properties == null ? new Properties() : properties;
        String targetType = properties.getProperty(Constants.CONVERTER_TARGET_COMMAND_SQL_TYPE_AS, node.getType());
        log.debug("nodeType: {}, targetType: {}", node.getType(), targetType);
        SetUtils.emptyIfNull(sqlTranslatorTypes).stream().map(clz -> {
            try {
                return clz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                log.warn("{}", e.getMessage());
            }
            return null;
        }).filter(Objects::nonNull).filter(translator -> translator.match(workflow, node)).forEach(translator -> {
            try {
                log.debug("translator: {}, rawType: {}, targetType: {}", translator.getClass(), node.getType(), targetType);
                String rawType = node.getType();
                node.setType(targetType);
                if (!translator.translate(workflow, node)) {
                    node.setType(rawType);
                } else {
                    changed.set(true);
                }
            } catch (org.apache.commons.cli.ParseException e) {
                throw new BizException(ErrorCode.TRANSLATE_NODE_ERROR).with(e);
            }
        });
        return changed.get();
    }
}
