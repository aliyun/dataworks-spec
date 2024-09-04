package com.aliyun.dataworks.common.spec.domain.ref.runtime.container;

import lombok.Data;

/**
 * container env var list
 * <a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#envvar-v1-core">kubernetes container env var spec</a>
 *
 * @author 戒迷
 * @date 2024/6/4
 */
@Data
public class SpecContainerEnvVar {
    /**
     * Name of the environment variable.
     */
    private String name;
    /**
     * environment variable's value
     */
    private String value;
}
