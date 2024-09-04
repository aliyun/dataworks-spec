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

package com.aliyun.dataworks.common.spec.domain.dw.codemodel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.alibaba.fastjson2.JSON;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/3/21
 */
@Slf4j
public class PaiCodeTest {
    @Test
    public void testPaiCode() throws IOException {
        String content = FileUtils.readFileToString(
            new File(Objects.requireNonNull(PaiCodeTest.class.getClassLoader().getResource("codemodel/pai_code_sample_1.json")).getFile()),
            StandardCharsets.UTF_8);

        CodeModel<Code> code = CodeModelFactory.getCodeModel(CodeProgramType.PAI.name(), null);
        code.setSourceCode(content);
        Assert.assertNotNull(code);
        PaiCode codeModel = (PaiCode)code.getCodeModel();
        Assert.assertNotNull(codeModel);
        Assert.assertNotNull(codeModel.getExtraContent());
        log.info("content: {}", codeModel.getContent());
        log.info("extraContent: {}", codeModel.getExtraContent());
        PaiCode paiCode = JSON.parseObject(codeModel.getContent(), PaiCode.class);
        Assert.assertNotNull(paiCode);
        Assert.assertEquals(23620, (long)paiCode.getAppId());
        Assert.assertNotNull(paiCode.getInputs());
        Assert.assertNotNull(paiCode.getOutputs());
    }
}
