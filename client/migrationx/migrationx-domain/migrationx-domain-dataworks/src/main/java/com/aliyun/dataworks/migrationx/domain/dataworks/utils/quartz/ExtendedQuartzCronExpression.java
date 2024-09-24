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

package com.aliyun.dataworks.migrationx.domain.dataworks.utils.quartz;

import java.text.ParseException;
import java.util.TreeSet;

/**
 * @author 聿剑
 * @date 2022/11/02
 */
public class ExtendedQuartzCronExpression extends CronExpression {
    public ExtendedQuartzCronExpression(String cronExpression) throws ParseException {
        super(cronExpression);
    }

    public ExtendedQuartzCronExpression(CronExpression expression) {
        super(expression);
    }

    public TreeSet<Integer> getSeconds() {
        return seconds;
    }

    public TreeSet<Integer> getMinutes() {
        return minutes;
    }

    public TreeSet<Integer> getHours() {
        return hours;
    }

    public TreeSet<Integer> getDaysOfMonth() {
        return daysOfMonth;
    }

    public TreeSet<Integer> getMonths() {
        return months;
    }

    public TreeSet<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public TreeSet<Integer> getYears() {
        return years;
    }

    public boolean getLastDayOfWeek() {
        return lastdayOfWeek;
    }

    public int getNthDayOfWeek() {
        return nthdayOfWeek;
    }

    public boolean getLastDayOfMonth() {
        return lastdayOfMonth;
    }

    public boolean getNearestWeekday() {
        return nearestWeekday;
    }

    public int getLastDayOffset() {
        return lastdayOffset;
    }
}
