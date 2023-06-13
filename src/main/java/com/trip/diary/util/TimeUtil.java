package com.trip.diary.util;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.LocalDateTime;

@UtilityClass
public class TimeUtil {
    public static String parseDate(LocalDateTime date) {
        LocalDateTime current = LocalDateTime.now();
        if (date.isAfter(current.minusYears(1))) {
            return date.getMonthValue() + "월 " + date.getDayOfMonth() +"일";
        }
        return (current.getYear() - date.getYear()) + "년 전";
    }
}
