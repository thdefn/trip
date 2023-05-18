package com.trip.diary.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.FileException;
import com.trip.diary.util.FilePathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3Client implements FileUploadClient {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    @Override
    public String upload(MultipartFile multipartFile, String domain) {
        String fileName = FilePathUtil.createFileName(multipartFile.getOriginalFilename());
        String filePath = FilePathUtil.createFilePath(domain);
        try {
            File file = File.createTempFile("image", fileName);
            multipartFile.transferTo(file);
            amazonS3.putObject(new PutObjectRequest(bucket, filePath + fileName, file));
        } catch (IOException e) {
            throw new FileException(ErrorCode.UPLOAD_FAILED);
        }
        return filePath + fileName;
    }

    @Override
    public void delete(String filePath) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, filePath));
    }
}
