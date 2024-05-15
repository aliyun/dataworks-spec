package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author 戒迷
 * @date 2024/4/16
 */
@Data
@Accessors(chain = true)
@ToString(exclude = {"fileContent"})
public class FileVersion {
    private Long fileId;
    private String fileContent;
    private String commitUser;
    private Integer fileType;
    private String changeType;
    private String fileName;
    private Long cloudUuid;
    private String comment;
    private String useType;
}
