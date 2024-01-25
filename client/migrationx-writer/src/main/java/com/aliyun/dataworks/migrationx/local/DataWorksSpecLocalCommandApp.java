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

package com.aliyun.dataworks.migrationx.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.Code;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.LabelType;
import com.aliyun.dataworks.common.spec.domain.dw.types.ModelTreeRoot;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRecurrenceType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.migrationx.common.command.appbase.CommandApp;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RegExUtils;

/**
 * @author 聿剑
 * @date 2023/9/21
 */
@Slf4j
public class DataWorksSpecLocalCommandApp extends CommandApp {
    public static void main(String[] args) throws Exception {
        new DataWorksSpecLocalCommandApp().run(args);
    }

    @Override
    public void run(String[] args) throws Exception {
        Options options = new Options();
        options.addRequiredOption("a", "action", true, "Action: CreateBusiness");

        CommandLine cli = getCommandLine(options, args);
        LocalAction action = LocalAction.valueOf(cli.getOptionValue("a"));
        switch (action) {
            case CREATE_BUSINESS: {
                Options bizOpt = new Options();
                bizOpt.addRequiredOption("n", "name", true, "Business name");
                cli = getCommandLine(bizOpt, cli.getArgs());
                doCreateBusiness(cli.getOptionValue("n"));
                break;
            }
            case CREATE_FLOW_SPEC: {
                Options flowOpt = new Options();
                flowOpt.addRequiredOption("f", "file", true, "File path");
                flowOpt.addRequiredOption("n", "nodeType", true, "Node type");
                cli = getCommandLine(flowOpt, cli.getArgs());
                String filePath = cli.getOptionValue("f");
                String nodeType = cli.getOptionValue("n");
                doCreateFlowSpec(filePath, nodeType);
                break;
            }
        }
    }

    private void doCreateFlowSpec(String filePath, String nodeType) throws IOException {
        File sourceFile = new File(filePath);
        if (!sourceFile.exists()) {
            throw new FileNotFoundException(filePath);
        }

        File flowSpecFile = new File(RegExUtils.replacePattern(filePath, "\\..*$", "") + ".flow");
        Specification<DataWorksWorkflowSpec> specification = new Specification<>();
        specification.setKind(SpecKind.CYCLE_WORKFLOW);
        specification.setVersion(SpecVersion.V_1_1_0);
        SpecNode specNode = new SpecNode();
        SpecScript specScript = new SpecScript();
        SpecScriptRuntime runtime = new SpecScriptRuntime();
        CodeProgramType prgType = CodeProgramType.valueOf(nodeType);
        runtime.setCommand(nodeType);
        runtime.setEngine(prgType.getCalcEngineType().getName());
        CodeModel<Code> code = CodeModelFactory.getCodeModel(nodeType, "");
        runtime.setTemplate(code.getTemplate());

        specScript.setRuntime(runtime);
        specScript.setPath(filePath);

        specNode.setScript(specScript);
        specNode.setName(RegExUtils.replacePattern(sourceFile.getName(), "\\..*$", ""));
        specNode.setInstanceMode(NodeInstanceModeType.T_PLUS_1);
        specNode.setRerunMode(NodeRerunModeType.ALL_ALLOWED);
        specNode.setRecurrence(NodeRecurrenceType.NORMAL);
        SpecTrigger trigger = new SpecTrigger();
        trigger.setType(TriggerType.SCHEDULER);
        trigger.setCron("day");
        specNode.setTrigger(trigger);
        specification.setSpec(new DataWorksWorkflowSpec());
        specification.getSpec().setNodes(Collections.singletonList(specNode));
        String spec = SpecUtil.writeToSpec(specification);

        FileUtils.writeStringToFile(flowSpecFile, spec, StandardCharsets.UTF_8);
        log.info("spec file: {} created", flowSpecFile.getAbsolutePath());
    }

    private void doCreateBusiness(String businessName) {
        Locale locale = getLocale();
        List<String> path = new ArrayList<>();
        path.add(ModelTreeRoot.BIZ_ROOT.getDisplayName(locale));
        path.add(businessName);
        File workflowDir = new File(Joiner.on(File.separator).join(path));
        if (!workflowDir.mkdirs()) {
            throw new RuntimeException("create business directory failed: " + workflowDir.getAbsolutePath());
        }

        log.info("Business directory: {} created", workflowDir.getAbsolutePath());
        Map<CalcEngineType, List<LabelType>> types = new HashMap<>();
        types.put(CalcEngineType.ODPS, Arrays.asList(LabelType.DATA_PROCESS, LabelType.RESOURCE, LabelType.FUNCTION));
        types.put(CalcEngineType.EMR, Arrays.asList(LabelType.DATA_PROCESS, LabelType.RESOURCE, LabelType.FUNCTION));
        types.put(CalcEngineType.HADOOP_CDH, Arrays.asList(LabelType.DATA_PROCESS, LabelType.RESOURCE, LabelType.FUNCTION));
        types.put(CalcEngineType.HOLO, Collections.singletonList(LabelType.DATA_PROCESS));
        types.put(CalcEngineType.CLICKHOUSE, Collections.singletonList(LabelType.DATA_PROCESS));
        types.put(CalcEngineType.ADB_MYSQL, Collections.singletonList(LabelType.DATA_PROCESS));
        types.put(CalcEngineType.HYBRIDDB_FOR_POSTGRESQL, Collections.singletonList(LabelType.DATA_PROCESS));
        types.put(CalcEngineType.FLINK, Collections.singletonList(LabelType.DATA_PROCESS));

        types.keySet().forEach(eng -> {
            File engineDir = new File(workflowDir, eng.getDisplayName(locale));
            if (!engineDir.mkdirs()) {
                throw new RuntimeException("create business engine directory failed: " + engineDir.getAbsolutePath());
            }
            log.info("Business engine directory: {} created", engineDir.getAbsolutePath());

            ListUtils.emptyIfNull(types.get(eng)).forEach(la -> {
                File labelDir = new File(engineDir, la.getDisplayName(locale));
                if (!labelDir.mkdirs()) {
                    throw new RuntimeException("create business engine label directory failed: " + labelDir.getAbsolutePath());
                }
                log.info("Business engine label directory: {} created", labelDir.getAbsolutePath());
            });
        });
    }
}
