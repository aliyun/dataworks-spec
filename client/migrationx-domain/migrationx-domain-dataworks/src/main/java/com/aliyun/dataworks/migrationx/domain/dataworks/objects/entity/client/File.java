/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author sam.liux
 * @date 2020/04/27
 */
@Data
@Accessors(chain = true)
@ToString(exclude = {"content"})
public class File {
    private Long appId;
    private Long bizId;
    private Long cloudUuid;
    private Integer commitStatus;
    private String connName;
    private String content;
    private Date createTime;
    private String createUser;
    private Integer currentVersion;
    private String extend;
    private String extraContent;
    private String fileDagUrl;
    private Integer fileDelete;
    private String fileDesc;
    private String fileFolderId;
    private String fileFolderPath;
    private Long fileId;
    private Integer fileLockStatus;
    private String fileLockUser;
    private String fileLockUserName;
    private String fileName;
    private Integer filePublish;
    private Integer fileType;
    private String galaxyResultTableSql;
    private String galaxySourceTableSql;
    private String galaxyTaskConfig;
    private FileInstanceInfo instanceInfo;
    private Integer isAutoParse;
    private Integer isLarge;
    private Boolean isOdps;
    private Integer isProtected;
    private Long labelId;
    private Date lastEditTime;
    private String lastEditUser;
    private String lastEditUserName;
    private Integer limit;
    private Integer locked;
    private Integer lockedBy;
    private String lockedByName;
    private Long nodeId;
    private String originResourceName;
    private String owner;
    private String ownerName;
    private Long parentId;
    private Integer parentType;
    private String position;
    private String reference;
    private String region;
    private String sourceApp;
    private Integer start;
    private Long tenantId;
    private String ttContent;
    private Integer useType;
    private String workspaceUrl;
    private Boolean ignoreLock;
}
