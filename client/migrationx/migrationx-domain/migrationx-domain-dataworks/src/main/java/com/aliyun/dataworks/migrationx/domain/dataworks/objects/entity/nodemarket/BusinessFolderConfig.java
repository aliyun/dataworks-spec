package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.nodemarket;

import java.util.List;
import java.util.Map;

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
public class BusinessFolderConfig {
    private String type;
    private String appName;
    private String engine;
    private Map<String, String> displayName;
    private BusinessFolder folders;
    private List<Integer> nodes;
}
