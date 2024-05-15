package com.aliyun.dataworks.common.spec.parser;

import java.util.Arrays;
import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.DataWorksTableSpec;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.parser.impl.SpecParser;

/**
 * @author 子梁
 * @date 2024/4/23
 */
public class DataWorksTableSpecParser extends SpecParser<DataWorksTableSpec> {

    @Override
    public boolean support(String kind) {
        return Arrays.stream(SpecKind.values()).map(SpecKind::getLabel).anyMatch(k -> k.equalsIgnoreCase(kind));
    }

    @Override
    public DataWorksTableSpec parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        DataWorksTableSpec specObj = instantiateSpecObject();
        specParserContext.setIgnoreMissingFields(true);
        parseSpecObjectFields(specObj, rawContext, specParserContext);
        return specObj;
    }

}
