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

package com.aliyun.migrationx.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 聿剑
 * @date 2023/01/12
 */
public class DateUtils {
    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public DateUtils() {
    }

    public static Date getNormalizedDate() {
        return getDate(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0, 0, 0);
    }

    public static Date getNormalizedDate(int year, int month, int day) {
        return getDate(year, month, day, 0, 0, 0, 0);
    }

    private static Date getDate(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        if (year != Integer.MIN_VALUE) {
            gc.set(1, year);
        }

        if (month != Integer.MIN_VALUE) {
            gc.set(2, month - 1);
        }

        if (day != Integer.MIN_VALUE) {
            gc.set(5, day);
        }

        if (hour != Integer.MIN_VALUE) {
            gc.set(11, hour);
        }

        if (minute != Integer.MIN_VALUE) {
            gc.set(12, minute);
        }

        if (second != Integer.MIN_VALUE) {
            gc.set(13, second);
        }

        if (millisecond != Integer.MIN_VALUE) {
            gc.set(14, millisecond);
        }

        return gc.getTime();
    }

    public static Date getCurrentDay() {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        gc.set(11, 0);
        gc.set(12, 0);
        gc.set(13, 0);
        gc.set(14, 0);
        return gc.getTime();
    }

    public static Date getCurrentTime() {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        return gc.getTime();
    }

    public static Date getCurrentDateTime(String pattern) {
        try {
            return convertStringTODate(getCurrentDateString(pattern), pattern);
        } catch (ParseException var2) {
            var2.printStackTrace();
            return Calendar.getInstance().getTime();
        }
    }

    public static Date convertStringTODate(String dateStr, String pattern) throws ParseException {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        } else {
            SimpleDateFormat sf = new SimpleDateFormat(pattern);
            return sf.parse(dateStr);
        }
    }

    public static String getCurrentDateString(String pattern) {
        return convertDateToString(getCurrentTime(), pattern);
    }

    public static Date getAfterDate(Date date) {
        return new Date(date.getTime() + 86400000L);
    }

    public static Date convertLongToDate(long timestamp) {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        gc.setTimeInMillis(timestamp);
        return gc.getTime();
    }

    public static long convertDateToLong(Date date) {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        gc.setTime(date);
        return gc.getTimeInMillis();
    }

    public static Date convertStringToDate(String dateStr) {
        return convertStringToDate(dateStr, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date convertStringToDate(String dateStr, String pattern) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        } else {
            try {
                SimpleDateFormat sf = new SimpleDateFormat(pattern);
                return sf.parse(dateStr);
            } catch (ParseException var3) {
                return null;
            }
        }
    }

    public static String convertDateToString(Date date) {
        return convertDateToString(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String convertDateToString(Date date, String pattern) {
        if (date == null) {
            return null;
        } else {
            SimpleDateFormat sf = new SimpleDateFormat(pattern);
            sf.setLenient(false);
            return sf.format(date);
        }
    }

    public static Date getTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(14, 0);
        return cal.getTime();
    }

    public static Date parse(String date, String format) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
            return localDateTime2Date(ldt);
        } catch (Exception e) {
            logger.error("error while parse date:" + date, e);
        }
        return null;
    }

    private static Date localDateTime2Date(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static String format(Date date, String format) {
        return format(date2LocalDateTime(date), format);
    }

    public static String format(LocalDateTime localDateTime, String format) {
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    private static LocalDateTime date2LocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static Date getFirstDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        return cal.getTime();
    }

    public static Date addDays(Date date, int amount) {
        return add(date, 5, amount);
    }

    public static Date addMinutes(Date date, int amount) {
        return add(date, 12, amount);
    }

    /**
     * get date
     *
     * @param date          date
     * @param calendarField calendarField
     * @param amount        amount
     * @return date
     */
    public static Date add(final Date date, final int calendarField, final int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    public static Date getLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);

        return cal.getTime();
    }

    public static Date getMonday(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        return cal.getTime();
    }

    public static Date getSunday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        return cal.getTime();
    }

    public static Date addMonths(Date date, int amount) {
        return add(date, 2, amount);
    }
}
