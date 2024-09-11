package com.aliyun.dataworks.common.spec.parser.impl;

import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.ref.runtime.container.SpecContainerEnvVar;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;

/**
 * @author 戒迷
 * @date 2024/6/6
 */
public class SpecContainerEnvVarParser extends DefaultSpecParser<SpecContainerEnvVar> {
    @Override
    public SpecContainerEnvVar parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        return super.parse(rawContext, specParserContext);
    }
}
