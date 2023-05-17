package com.trip.diary.client;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadClient {
    String upload(MultipartFile multipartFile, String domain) throws IOException;

    void delete(String filePath);
}
