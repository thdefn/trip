package com.trip.diary.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@UtilityClass
public class FilePathUtil {
    public String createFileName(String fileName) {
        return UUID.randomUUID().toString().replaceAll("-", "") +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) +
                getFileExtension(fileName);
    }

    public String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public String createFilePath(String domain) {
        return domain + "/" + LocalDate.now() + "/";
    }
}
