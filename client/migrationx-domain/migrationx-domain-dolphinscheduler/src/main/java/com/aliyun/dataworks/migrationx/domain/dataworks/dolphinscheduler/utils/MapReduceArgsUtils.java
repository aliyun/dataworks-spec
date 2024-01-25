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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.mr.MapReduceParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.ProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.ResourceInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * mapreduce args utils
 */
public class MapReduceArgsUtils {

    private MapReduceArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * build args
     *
     * @param param param
     * @return argument list
     */
    public static List<String> buildArgs(MapReduceParameters param) {
        List<String> args = new ArrayList<>();

        ResourceInfo mainJar = param.getMainJar();
        if (mainJar != null) {
            args.add(mainJar.getName());
        }

        ProgramType programType = param.getProgramType();
        String mainClass = param.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            args.add(mainClass);
        }

        String others = param.getOthers();
        // -conf -archives -files -libjars -D
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        String mainArgs = param.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        return args;
    }

}
