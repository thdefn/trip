package com.trip.diary.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class FileUtil {
    public static String createFileName(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder
                .append(UUID.randomUUID().toString().replaceAll("-", ""))
                .append(LocalDateTime.now())
                .append(getFileExtension(fileName))
                .toString();
    }

    private static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static String createFilePath(String domain) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder
                .append("/")
                .append(LocalDate.now())
                .append("/")
                .toString();
    }
}
