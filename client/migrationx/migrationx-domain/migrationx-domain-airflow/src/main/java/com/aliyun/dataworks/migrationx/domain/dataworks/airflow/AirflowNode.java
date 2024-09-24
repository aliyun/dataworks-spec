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

package com.aliyun.dataworks.migrationx.domain.dataworks.airflow;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author sam.liux
 * @date 2021/01/21
 */
@Data
@ToString(callSuper = true)
@Accessors(chain = true)
public class AirflowNode {
    private String name;
    private String type;
    private String cronExpress;
    private String code;
    private String parameters;
    private List<DwNodeIo> inputs;
    private List<DwNodeIo> outputs;
    private JsonObject airflowTask;
    private String exception;
}
