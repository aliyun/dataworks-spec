package com.aliyun.dataworks.common.spec.domain;

import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import lombok.Data;

/**
 * @author 子梁
 * @date 2024/4/23
 */
@Data
public class DataWorksTableSpec extends SpecTable implements Spec {
    @Override
    public List<SpecKind> getKinds() {
        return Collections.singletonList(SpecKind.TABLE);
    }
}
