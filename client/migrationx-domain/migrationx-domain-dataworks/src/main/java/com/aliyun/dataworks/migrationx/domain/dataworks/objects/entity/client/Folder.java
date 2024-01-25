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
import java.util.List;

/**
 * @author sam.liux
 * @date 2020/05/12
 */
@Data
@ToString
@Accessors(chain = true)
public class Folder {
    private Integer folderItemType;
    private String folderItemName;
    private String folderItemPath;
    private String bizId;
    private Integer bizUseType;
    private Integer type;
    private Integer subType;
    private String sourceApp;
    private Integer version;
    private String engineType;
    private String absolutePath;
    private Long appId;
    private String displayName;
    private Integer fileCnt;
    private String folderId;
    private Date folderItemCreatetime;
    private String folderItemCreator;
    private String folderItemUpdater;
    private Date folderItemUpdatetime;
    private Integer folderLabelId;
    private Long id;
    private Integer index;
    private List<Integer> labelIdList;
    private String labelIds;
    private Integer limit;
    private Integer locked;
    private String lockedBy;
    private String lockedByName;
    private String parentFolderItemId;
    private String parentGroupId;
    private Integer parentLabelId;
    private Integer queryType;
    private Integer srcType;
    private Integer start;
    private Long tenantId;
}
