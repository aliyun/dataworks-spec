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

package com.aliyun.dataworks.common.spec.domain.dw.types;

import java.util.Locale;

import lombok.Getter;

@Getter
public enum ModelTreeRoot implements LocaleAware {
    ALL_ROOT("allroot", "根目录", "", "allroot"),
    BIZ_ROOT("bizroot", "业务流程", "Business Flow", "biz"),
    MANUAL_BIZ_ROOT("manualbizroot", "手动业务流程", "Manual Business Flow", "manualbiz"),
    WORK_FLOW_ROOT_NEW("workflowroot", "旧版工作流", "Workflow", "workflow"),
    WORK_FLOW_ROOT("workflowroot", "工作流", "Workflow", "workflow"),
    MANUAL_WORK_FLOW_ROOT("manualworkflowroot", "手动工作流", "Manual Workflow", "manualworkflow"),
    ONCE_ROOT("onceroot", "手动任务", "Manual Tasks", "manualtask"),
    QUERY_ROOT("queryroot", "临时查询", "Queries", "adhocquery"),
    COMPONENT_ROOT("componentroot", "组件", "Components", ""),

    ;

    private final String rootKey;
    private final String name;
    private final String englishName;
    private final String module;

    ModelTreeRoot(String rootKey, String name, String englishName, String module) {
        this.rootKey = rootKey;
        this.name = name;
        this.englishName = englishName;
        this.module = module;
    }

    public static ModelTreeRoot searchModelTreeRoot(String keyword) {
        for (ModelTreeRoot root : ModelTreeRoot.values()) {
            if (root.equals(ALL_ROOT)) {
                continue;
            }
            if (root.getRootKey().equalsIgnoreCase(keyword)
                || root.getName().equalsIgnoreCase(keyword)
                || root.getEnglishName().equalsIgnoreCase(keyword)) {
                return root;
            }
        }
        return null;
    }

    public boolean matches(String keyword) {
        return this.getRootKey().equalsIgnoreCase(keyword) ||
            this.getName().equalsIgnoreCase(keyword) ||
            this.getEnglishName().equalsIgnoreCase(keyword) ||
            this.getModule().equalsIgnoreCase(keyword);
    }

    @Override
    public String getDisplayName(Locale locale) {
        if (Locale.SIMPLIFIED_CHINESE.equals(locale)) {
            return getName();
        }

        return getEnglishName();
    }
}