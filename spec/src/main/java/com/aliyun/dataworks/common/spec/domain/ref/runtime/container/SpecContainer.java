package com.aliyun.dataworks.common.spec.domain.ref.runtime.container;

import java.util.List;

import com.aliyun.dataworks.common.spec.domain.SpecEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * runtime container
 * <a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#container-v1-core">kubernetes container core spec</a>
 *
 * @author 戒迷
 * @date 2024/6/4
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpecContainer extends SpecEntity {
    /**
     * Arguments to the entrypoint. The docker image's CMD is used if this is not provided.
     */
    private List<String> args;
    /**
     * Entrypoint array. Not executed within a shell. The docker image's ENTRYPOINT is used if this is not provided.
     */
    private List<String> command;
    /**
     * Docker image name.
     */
    private String image;
    /**
     * Docker image id
     */
    private String imageId;
    /**
     * List of environment variables to set in the container.
     */
    private List<SpecContainerEnvVar> env;
}
