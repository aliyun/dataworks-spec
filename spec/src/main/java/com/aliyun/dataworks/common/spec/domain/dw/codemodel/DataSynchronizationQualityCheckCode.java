package com.aliyun.dataworks.common.spec.domain.dw.codemodel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2025-10-27
 */
@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DataSynchronizationQualityCheckCode extends AbstractBaseCode implements JsonFormCode {

    private transient JsonObject jsonObject;

    @Override
    public DataSynchronizationQualityCheckCode parse(String code) {
        jsonObject = GsonUtils.fromJsonString(code, JsonObject.class);
        return this;
    }

    @Override
    public Map<String, Object> getTemplate() {
        return null;
    }

    @Override
    public List<String> getProgramTypes() {
        return Collections.singletonList(CodeProgramType.DATA_SYNCHRONIZATION_QUALITY_CHECK.name());
    }

    @Override
    public String getContent() {
        if (this.jsonObject == null) {
            return null;
        }

        return GsonUtils.defaultGson.toJson(jsonObject);
    }
}
