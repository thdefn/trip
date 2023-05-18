package com.trip.diary.util;

import com.trip.diary.client.FileUploadClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ImageManager {
    private final FileUploadClient fileUploadClient;
    public static final List<String> ALLOW_IMAGE_CODES = List.of(".jpeg", ".png", ".jpg", ".gif");

    public List<String> uploadImages(List<MultipartFile> multipartFiles, String domain) {
        return multipartFiles.stream()
                .filter(multipartFile ->
                        ALLOW_IMAGE_CODES.contains(
                                FilePathUtil.getFileExtension(
                                        Objects.requireNonNull(multipartFile.getOriginalFilename()))))
                .map(multipartFile -> fileUploadClient.upload(multipartFile, domain))
                .collect(Collectors.toList());
    }

    public void deleteImages(List<String> imagePaths){
        imagePaths.forEach(fileUploadClient::delete);
    }
}
