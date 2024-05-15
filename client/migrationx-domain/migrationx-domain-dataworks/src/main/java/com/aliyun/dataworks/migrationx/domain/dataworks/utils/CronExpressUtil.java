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

package com.aliyun.dataworks.migrationx.domain.dataworks.utils;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CycleType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.quartz.CronExpression;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.quartz.ExtendedQuartzCronExpression;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sam.liux
 * @date 2021/05/31
 */
public class CronExpressUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CronExpressUtil.class);
    private static final String WILDCARD_STAR = "*";
    private static final String WILDCARD_QUESTION_MARK = "?";
    private static final String SLASH = "/";
    private static final String DASH = "-";
    private static final String COMMA = ",";
    public static final String LAST_DAY_OF_MONTH = "L";

    private static final int MIN_PARTS_COUNT = 7;
    private static final List<Integer> NO_NEED_NORMALIZE = Arrays.asList(
        CronExpression.DAY_OF_WEEK, CronExpression.DAY_OF_MONTH, CronExpression.MONTH);

    public static String quartzCronExpressionToDwCronExpress(String cronExpress) throws ParseException {
        if (StringUtils.isBlank(cronExpress)) {
            return cronExpress;
        }

        String[] parts = StringUtils.split(cronExpress, " \t");
        if (parts == null || parts.length < MIN_PARTS_COUNT) {
            LOGGER.warn("invalid quartz cron part count: {}, {}",
                cronExpress, Optional.ofNullable(parts).map(p -> p.length).orElse(null));
            return cronExpress;
        }

        ExtendedQuartzCronExpression cronExpression = new ExtendedQuartzCronExpression(cronExpress);
        List<String> dwCron = new ArrayList<>();
        List<String> quartz = Arrays.asList(parts);
        // init data
        for (int i = 0; i < parts.length; i++) {
            dwCron.add("");
        }

        for (int i = 0; i < parts.length; i++) {
            processParts(i, quartz, cronExpression, dwCron);
        }

        return StringUtils.trim(Joiner.on(" ").join(dwCron));
    }

    private static void processParts(int partIndex, List<String> quartz,
                                     ExtendedQuartzCronExpression cronExpression, List<String> dwCron) {
        switch (partIndex) {
            case CronExpression.SECOND:
            case CronExpression.MINUTE:
            case CronExpression.HOUR:
            case CronExpression.MONTH:
            case CronExpression.DAY_OF_WEEK:
                processCommonPart(partIndex, quartz, cronExpression, dwCron);
                break;
            case CronExpression.DAY_OF_MONTH:
                processDayOfMonth(partIndex, quartz, cronExpression, dwCron);
                break;
            default:
        }

        if (StringUtils.equalsIgnoreCase(WILDCARD_STAR, dwCron.get(CronExpression.DAY_OF_WEEK))) {
            dwCron.set(CronExpression.DAY_OF_WEEK, WILDCARD_QUESTION_MARK);
        }
    }

    private static void processDayOfMonth(int partIndex, List<String> quartz,
                                          ExtendedQuartzCronExpression cronExpression, List<String> dwCron) {
        if (isCommonExpr(partIndex, quartz)) {
            processCommonPart(partIndex, quartz, cronExpression, dwCron);
            return;
        }

        if (StringUtils.equalsIgnoreCase(WILDCARD_QUESTION_MARK, quartz.get(partIndex))) {
            String dayOfWeek = quartz.get(CronExpression.DAY_OF_WEEK);
            /**
             * '2/3' - 每隔3天，从星期一开始
             * 'SUM,MON,TUE' - 星期几集合
             * '4L' - 在这个月的最后一个星期三
             * '4#2' - 在这个月的第2个星期三
             */
            LOGGER.info("dayOfWeek: {}", dayOfWeek);
            dwCron.set(partIndex, WILDCARD_QUESTION_MARK);
            processTreeSetOfEnumeratedValues(CronExpression.DAY_OF_WEEK, dwCron, dwCron,
                cronExpression.getDaysOfWeek());
            return;
        }

        /**
         * 'L' - 在这个月的最后一天
         * 'LW' - 在这个月的最后一个工作日
         * 'L-1' - 在本月底前1天
         * '1W' - 最近的工作日（周一至周五）至本月1日
         *
         */
        if (StringUtils.equalsIgnoreCase(LAST_DAY_OF_MONTH, quartz.get(partIndex))) {
            dwCron.set(partIndex, LAST_DAY_OF_MONTH);
        } else {
            LOGGER.warn("got dataworks unsupported cron expression part: {}, set as *", quartz.get(partIndex));
            dwCron.set(partIndex, WILDCARD_STAR);
        }
    }

    private static boolean isCommonExpr(int partIndex, List<String> quartz) {
        return StringUtils.equalsIgnoreCase(WILDCARD_STAR, quartz.get(partIndex)) ||
            StringUtils.indexOf(quartz.get(partIndex), SLASH) >= 0 ||
            StringUtils.indexOf(quartz.get(partIndex), DASH) >= 0 ||
            StringUtils.indexOf(quartz.get(partIndex), COMMA) >= 0;
    }

    /**
     * '*' '3/5' '1,2,3,4,5,6,7' '1-6'
     *
     * @param partIndex
     * @param quartz
     * @param cronExpression
     * @param dwCron
     */
    private static void processCommonPart(int partIndex, List<String> quartz,
                                          ExtendedQuartzCronExpression cronExpression, List<String> dwCron) {
        if (StringUtils.isNumeric(quartz.get(partIndex))) {
            dwCron.set(partIndex, normalizePart(quartz.get(partIndex)));
            return;
        }

        TreeSet<Integer> set = new TreeSet<>();
        switch (partIndex) {
            case CronExpression.SECOND: {
                dwCron.set(partIndex, normalizePart("0"));
                break;
            }
            case CronExpression.MINUTE: {
                set = cronExpression.getMinutes();
                if (isWildcard(quartz.get(partIndex))) {
                    dwCron.set(partIndex, "*/5");
                } else {
                    processTreeSetOfEnumeratedValues(partIndex, quartz, dwCron, set, 5);
                }
                break;
            }
            case CronExpression.HOUR: {
                if (!StringUtils.isNumeric(quartz.get(partIndex))) {
                    dwCron.set(CronExpression.MINUTE, normalizePart("0"));
                }

                set = cronExpression.getHours();
                if (isWildcard(quartz.get(partIndex))) {
                    dwCron.set(partIndex, "00-23/1");
                } else {
                    processTreeSetOfEnumeratedValues(partIndex, quartz, dwCron, set);
                }
                break;
            }
            case CronExpression.MONTH: {
                set = cronExpression.getMonths();
                if (!isWildcard(quartz.get(partIndex))) {
                    // 月调度
                    dwCron.set(CronExpression.HOUR, normalizePart("0"));
                    processTreeSetOfEnumeratedValues(CronExpression.DAY_OF_MONTH, quartz, dwCron,
                        cronExpression.getDaysOfMonth());
                }
                break;
            }
            case CronExpression.DAY_OF_WEEK: {
                set = cronExpression.getDaysOfWeek();
                if (!isWildcard(quartz.get(partIndex))) {
                    // 周调度
                    dwCron.set(CronExpression.HOUR, normalizePart("0"));
                }
                break;
            }
            case CronExpression.DAY_OF_MONTH: {
                // 日调度
                set = cronExpression.getDaysOfMonth();
                if (!isWildcard(quartz.get(partIndex))) {
                    dwCron.set(CronExpression.HOUR, normalizePart("0"));
                }
                break;
            }
            default:
        }

        if (StringUtils.isBlank(dwCron.get(partIndex))) {
            if (isWildcard(quartz.get(partIndex))) {
                dwCron.set(partIndex, WILDCARD_STAR);
            } else {
                processTreeSetOfEnumeratedValues(partIndex, quartz, dwCron, set);
            }
        }
    }

    private static boolean isWildcard(String part) {
        if (Stream.of(WILDCARD_STAR, WILDCARD_QUESTION_MARK).anyMatch(s -> StringUtils.equalsIgnoreCase(s, part))) {
            return true;
        }
        return false;
    }

    private static void processTreeSetOfEnumeratedValues(int partIndex, List<String> quartz, List<String> dwCron,
                                                         TreeSet<Integer> set) {
        processTreeSetOfEnumeratedValues(partIndex, quartz, dwCron, set, null);
    }

    private static void processTreeSetOfEnumeratedValues(int partIndex, List<String> quartz, List<String> dwCron,
                                                         TreeSet<Integer> set, Integer minRate) {
        boolean fixRate = true;
        Iterator<Integer> itr = set.iterator();
        Integer last = null;
        Integer lastDelta = null;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        do {
            int cur = itr.next();

            if (min > cur) {
                min = cur;
            }
            if (max < cur) {
                max = cur;
            }

            if (lastDelta != null && cur - last != lastDelta) {
                fixRate = false;
                break;
            }

            if (last != null) {
                lastDelta = cur - last;
            }

            last = cur;
        } while (itr.hasNext());

        if (!NO_NEED_NORMALIZE.contains(partIndex) && lastDelta != null && fixRate) {
            int finalLastDelta = lastDelta;
            dwCron.set(partIndex, normalizePart(
                String.valueOf(min)) + DASH + normalize(String.valueOf(max)) + SLASH +
                Optional.ofNullable(minRate).map(r -> Math.max(r, finalLastDelta)).orElse(lastDelta));
        } else {
            dwCron.set(partIndex, SetUtils.emptyIfNull(set).stream()
                .filter(val -> (char)val.intValue() != CronExpression.ALL_SPEC_INT)
                .filter(val -> (char)val.intValue() != CronExpression.NO_SPEC_INT)
                .map(val -> NO_NEED_NORMALIZE.contains(partIndex) ? Integer.toString(val)
                    : normalizePart(Integer.toString(val)))
                .collect(Collectors.joining(COMMA)));
            if (StringUtils.isBlank(dwCron.get(partIndex))) {
                LOGGER.warn("got dataworks unsupported cron expression part: {}", quartz.get(partIndex));
                dwCron.set(partIndex, WILDCARD_QUESTION_MARK);
            }
        }
    }

    public static String normalize(String cronExpress) {
        if (StringUtils.isBlank(cronExpress)) {
            return cronExpress;
        }

        String[] parts = StringUtils.split(cronExpress, " ");
        if (parts == null || parts.length <= 0) {
            return cronExpress;
        }

        /**
         * ide调度属性前3个部分是2位数： aa bb cc-dd/e f g h
         */
        for (int i = 0; i < Math.min(3, parts.length); i++) {
            String partStr = parts[i];
            try {
                parts[i] = normalizePart(partStr);
            } catch (Exception e) {
                LOGGER.warn("normalize part error: ", e);
            }
        }

        return Joiner.on(" ").join(parts);
    }

    /**
     * 加前导零
     *
     * @param part
     * @return
     */
    private static String normalizePart(String part) {
        if (StringUtils.isNumeric(part)) {
            Integer value = Integer.valueOf(part);
            return String.format("%02d", value);
        }

        /**
         * xx-yy/z
         */
        if (StringUtils.contains(part, DASH)) {
            String[] parts = StringUtils.split(part, DASH);
            if (parts == null || parts.length < 2) {
                return part;
            }

            Integer partFrom = Integer.valueOf(parts[0]);
            Integer partTo = null;
            Integer partInterval = null;
            if (StringUtils.contains(parts[1], SLASH)) {
                String[] moreParts = StringUtils.split(parts[1], SLASH);
                if (moreParts == null || moreParts.length < 2) {
                    return part;
                }

                partTo = Integer.valueOf(moreParts[0]);
                partInterval = Integer.valueOf(moreParts[1]);
                return String.format("%02d-%02d/%d", partFrom, partTo, partInterval);
            } else {
                partTo = Integer.valueOf(parts[1]);
                return String.format("%02d-%02d", partFrom, partTo);
            }
        }
        return part;
    }


    public static Integer parseCronToCycleType(String cronExpression) {
        if (StringUtils.isBlank(cronExpression) || "day".equalsIgnoreCase(cronExpression)) {
            return CycleType.DAY.getCode();
        }

        try {
            String[] cronExp = cronExpression.split("\\s+");
            String pattern = "[-/,*]";
            Pattern regex = Pattern.compile(pattern);

            String minute = cronExp[1];
            String hour = cronExp[2];

            Matcher matchM = regex.matcher(minute);
            Matcher matchH = regex.matcher(hour);

            if (matchM.find() || matchH.find()) {
                //计算得到的小时调度和分钟调度才重新算cycleType
                return CycleType.NOT_DAY.getCode();
            } else {
                return CycleType.DAY.getCode();
            }
        } catch (Exception e) {
            return CycleType.DAY.getCode();
        }
    }
}
