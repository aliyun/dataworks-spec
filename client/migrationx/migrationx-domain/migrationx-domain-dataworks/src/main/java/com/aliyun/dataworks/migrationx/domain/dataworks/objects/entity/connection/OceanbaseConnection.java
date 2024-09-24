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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.connection;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author sam.liux
 * @date 2021/01/07
 */
@ToString(callSuper = true)
@Data
@Accessors(chain = true)
public class OceanbaseConnection extends JdbcConnection {
    private String clusterId;
    private String ownerId;
    private String tenant;
}