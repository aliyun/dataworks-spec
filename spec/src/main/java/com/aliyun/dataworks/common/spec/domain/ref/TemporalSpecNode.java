package com.aliyun.dataworks.common.spec.domain.ref;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TemporalSpecNode extends SpecNode {
    private String originSource;
}
