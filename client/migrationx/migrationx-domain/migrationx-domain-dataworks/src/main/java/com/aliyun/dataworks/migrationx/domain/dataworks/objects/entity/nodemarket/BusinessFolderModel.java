package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.nodemarket;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author sam.liux
 * @date 2020/02/26
 */
@Data
@ToString
@Accessors
public class BusinessFolderModel {
    private String appName;
    private String engineName;
    private Integer version;
    private BusinessFolderConfig configValue;
}
