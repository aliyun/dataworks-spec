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

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CrossTenantCode.Receiver;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/3/14
 */
@Slf4j
public class CrossTenantCodeTest {
    @Test
    public void testSenderCodeMode() {
        String code
            = "{\n"
            + "    \"actionType\": \"send\",\n"
            + "    \"nodeIdentify\": \"path/to/node\",\n"
            + "    \"receiveNodeIdentify\": \"\",\n"
            + "    \"receiveTimeout\": 30,\n"
            + "    \"nodeId\": \"\",\n"
            + "    \"receivers\": [\n"
            + "        {\n"
            + "            \"receiverAccount\": \"adasdf\",\n"
            + "            \"receiverProject\": \"sdfadf\"            \n"
            + "        }\n"
            + "    ]\n"
            + "  }";
        CodeModel<CrossTenantCode> codeModel
            = CodeModelFactory.getCodeModel(CodeProgramType.CROSS_TENANTS.name(), code);
        log.info("code model: {}", codeModel);
        Assert.assertNotNull(codeModel.getCodeModel());

        CrossTenantCode codeMode = codeModel.getCodeModel();
        log.info("new content: {}", codeMode.getContent());

        codeMode.setReceiveTimeout(100);
        Receiver recv = new Receiver();
        recv.setReceiverAccount("xxx");
        recv.setReceiverProject("yyy");
        codeMode.getReceivers().add(recv);
        log.info("new content: {}", codeMode.getContent());

        Assert.assertTrue(StringUtils.indexOf(codeMode.getContent(), "\"receiveTimeout\": 100,") > 0);
        Assert.assertTrue(StringUtils.indexOf(codeMode.getContent(), "xxx") > 0);
        Assert.assertTrue(StringUtils.indexOf(codeMode.getContent(), "yyy") > 0);

    }
}
