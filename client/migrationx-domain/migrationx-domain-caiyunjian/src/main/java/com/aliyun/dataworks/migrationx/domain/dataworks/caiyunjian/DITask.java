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

package com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian;

import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-07
 */
@Data
public class DITask {
    private JsonObject extend;
    private String type = "job";
    private String version = "2.0";
    private List<Step> steps;
    private Order order;
    @SerializedName(value = "setting", alternate = {"dataxSetting"})
    private Setting setting;

    public DITask() {
        order = new Order();
        setting = new Setting();
        steps = new ArrayList<>();
    }

    @Data
    public static class Step {
        private String stepType;
        private JsonObject parameter;
        private String name;
        private String category;

        public Step(String stepType, JsonObject parameter, String name, String category) {
            this.stepType = stepType;
            this.parameter = parameter;
            this.name = name;
            this.category = category;
        }

        public Step() {
        }

        ;
    }

    @Data
    public static class Order {
        private List<Hop> hops;

        private Order() {
            hops = Collections.singletonList(new Hop());
        }

        @Data
        public static class Hop {
            private String from = "Reader";
            private String to = "Writer";
        }

    }

    @Data
    public static class Setting {
        private ErrorLimit errorLimit;
        private Speed speed;

        public Setting() {
            errorLimit = new ErrorLimit();
            speed = new Speed();
        }

        @Data
        public static class ErrorLimit {
            private Integer record;
        }

        @Data
        public static class Speed {
            /**
             * 是否限流
             */
            private Boolean throttle = Boolean.FALSE;

            /**
             * 限流速率M/s
             */
            private Integer mbps;
            /**
             * 并发数
             */
            private Integer concurrent = 2;
            /**
             * dmu
             */
            private Integer dmu = 2;
        }
    }

    @Data
    public static class Connection {
        private String datasource;
        private String[] table;
    }

    @Override
    public String toString() {
        return GsonUtils.defaultGson.toJson(this);
    }
}
