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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2;

import java.util.List;

/**
 * @author sam.liux
 * @date 2019/07/17
 */
public class IdeNodeExtend {
    private IdeFile mainJar;
    private IdeFile mainPy;
    private List<IdeFile> assistJars;
    private List<IdeFile> assistFiles;
    private List<IdeFile> assistArchives;
    private List<IdeFile> assistPys;
    private String mode;
    private String resourceGroup;

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public IdeFile getMainJar() {
        return mainJar;
    }

    public void setMainJar(IdeFile mainJar) {
        this.mainJar = mainJar;
    }

    public IdeFile getMainPy() {
        return mainPy;
    }

    public void setMainPy(IdeFile mainPy) {
        this.mainPy = mainPy;
    }

    public List<IdeFile> getAssistPys() {
        return assistPys;
    }

    public void setAssistPys(List<IdeFile> assistPys) {
        this.assistPys = assistPys;
    }

    public List<IdeFile> getAssistJars() {
        return assistJars;
    }

    public void setAssistJars(List<IdeFile> assistJars) {
        this.assistJars = assistJars;
    }

    public List<IdeFile> getAssistFiles() {
        return assistFiles;
    }

    public void setAssistFiles(List<IdeFile> assistFiles) {
        this.assistFiles = assistFiles;
    }

    public List<IdeFile> getAssistArchives() {
        return assistArchives;
    }

    public void setAssistArchives(List<IdeFile> assistArchives) {
        this.assistArchives = assistArchives;
    }
}
