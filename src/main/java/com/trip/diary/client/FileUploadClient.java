package com.trip.diary.client;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadClient {
    String upload(MultipartFile multipartFile, String domain);

    void delete(String filePath);
}
