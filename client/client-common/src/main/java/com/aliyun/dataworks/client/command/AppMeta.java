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

package com.aliyun.dataworks.client.command;

import com.alibaba.fastjson2.JSONObject;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * App Meta Info
 *
 * @author 聿剑
 * @date 2023/03/08
 */
@Data
@ToString
@Accessors(chain = true)
public class AppMeta {
    private String name;
    private AppType type;
    private String appClass;
    private JSONObject config;
}
