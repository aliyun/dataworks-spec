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

package com.aliyun.dataworks.migrationx.transformer.core.checkpoint.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.transformer.core.checkpoint.CheckPoint;
import com.aliyun.migrationx.common.context.TransformerContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalFileCheckPoint implements CheckPoint<BufferedFileWriter> {
    /**
     * check point file suffix
     */
    public static final String SUFFIX = ".ckpt";
    /**
     * need append line to file
     */
    private static final boolean FILE_APPEND = true;
    private final static TypeReference<List<DwWorkflow>> REF = new TypeReference<List<DwWorkflow>>() {};
    private static final PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(DwNode.class)
            .allowIfSubType(DwResource.class)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper().setPolymorphicTypeValidator(ptv);

    @Override
    public List<DwWorkflow> doWithCheckpoint(Function<BufferedFileWriter, List<DwWorkflow>> checkpointFunc, String projectName) {
        File checkpoint = TransformerContext.getContext().getCheckpoint();
        if (checkpoint == null) {
            return checkpointFunc.apply(null);
        }
        File target = new File(checkpoint.getAbsolutePath() + File.separator + projectName + SUFFIX);
        return doWithCheckpoint(checkpointFunc, target);
    }

    public List<DwWorkflow> doWithCheckpoint(Function<BufferedFileWriter, List<DwWorkflow>> checkpointFunc, File target) {
        try (FileOutputStream out = new FileOutputStream(target, FILE_APPEND)) {
            try (OutputStreamWriter streamWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                try (BufferedFileWriter writer = new BufferedFileWriter(streamWriter)) {
                    return checkpointFunc.apply(writer);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doCheckpoint(BufferedFileWriter writer, List<DwWorkflow> workflows, String processName, String taskName) {
        if (writer == null) {
            return;
        }
        Map<String, List<DwWorkflow>> dataMap = new HashMap<>();
        dataMap.put(taskName, workflows);
        String data = toJson(workflows);
        try {
            writer.writeLine(data, processName, taskName);
            log.info("successful write checkpoint with project: {} processName: {} task: {}",
                    processName, taskName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<DwWorkflow>> loadFromCheckPoint(String projectName, String processName) {
        File dir = TransformerContext.getContext().getLoad();
        Map<String, List<DwWorkflow>> workflowMap = null;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            for (File checkFile : dir.listFiles()) {
                if (checkFile.getName().endsWith(projectName + SUFFIX)) {
                    workflowMap = loadFromCheckpoint(checkFile, processName);
                }
            }
        }
        if (workflowMap == null) {
            workflowMap = Collections.EMPTY_MAP;
        }
        return workflowMap;
    }

    private interface LineConsumer {
        void consume(String processName, String taskName, String line);
    }

    private Map<String, List<DwWorkflow>> loadFromCheckpoint(File checkpointFile, String qProcessName) {
        final Map<String, List<DwWorkflow>> workflowMap = new HashMap<>();
        final LineConsumer lineConsumer = (processName, taskName, line) -> {
            if (qProcessName.equals(processName)) {
                List<DwWorkflow> taskMap = parseJson(line);
                workflowMap.putIfAbsent(taskName, taskMap);
            }
        };
        doLoad(checkpointFile, lineConsumer);
        return workflowMap;
    }

    private void doLoad(File checkpointFile, LineConsumer lineConsumer) {
        try (FileInputStream in = new FileInputStream(checkpointFile)) {
            try (InputStreamReader streamReader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                try (BufferedReader reader = new BufferedReader(streamReader)) {
                    readFile(reader, lineConsumer);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFile(BufferedReader reader, LineConsumer consumer) throws IOException {
        int len;
        while ((len = reader.read()) != -1) {
            readFile(reader, len, consumer);
        }
    }

    private void readFile(BufferedReader reader, int len, LineConsumer lineConsumer) throws IOException {
        char[] processBytes = new char[len];
        reader.read(processBytes);
        String processName = new String(processBytes);
        len = reader.read();
        char[] taskBytes = new char[len];
        reader.read(taskBytes);
        String taskName = new String(taskBytes);
        String line = reader.readLine();
        lineConsumer.consume(processName, taskName, line);
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<DwWorkflow> parseJson(String json) {
        try {
            List<DwWorkflow> lists = objectMapper.readerFor(REF).readValue(json);
            return lists;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
