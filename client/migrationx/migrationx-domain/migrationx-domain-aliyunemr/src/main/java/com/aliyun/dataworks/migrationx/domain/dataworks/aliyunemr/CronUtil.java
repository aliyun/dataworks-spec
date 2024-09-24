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

package com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2024/5/11
 */
@Slf4j
public class CronUtil {
    public static String cronToDwCron(String cron) {
        if (StringUtils.isBlank(cron)) {
            return cron;
        }

        String[] parts = StringUtils.split(cron, " \t");
        if (parts == null || parts.length != 6) {
            log.warn("invalid quartz cron part count: {}", cron);
            return cron;
        }

        String week = parts[5];
        if (StringUtils.isNumeric(week)) {
            parts[5] = String.valueOf(rotateWeek(Integer.parseInt(week)));
        } else if (StringUtils.contains(week, ",")) {
            String[] weeks = StringUtils.split(week, ",");
            parts[5] = Stream.of(weeks).map(Integer::valueOf).map(CronUtil::rotateWeek).map(String::valueOf).collect(Collectors.joining(","));
        } else if (StringUtils.contains(week, "-")) {
            String[] weeks = StringUtils.split(week, "-");
            if (weeks.length == 2) {
                parts[5] = rotateWeek(Integer.parseInt(weeks[0])) + "-" + rotateWeek(Integer.parseInt(weeks[1]));
            }
        }
        return String.join(" ", parts);
    }

    private static int rotateWeek(int week) {
        switch (week) {
            case 1:
                return 7;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return 5;
            case 7:
                return 6;
            default:
                return week;
        }
    }
}
