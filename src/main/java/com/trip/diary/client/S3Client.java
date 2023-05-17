package com.trip.diary.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.trip.diary.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public class S3Client implements FileUploadClient {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    @Override
    public String upload(MultipartFile multipartFile, String domain) throws IOException {
        String fileName = FileUtil.createFileName(multipartFile.getOriginalFilename());
        String filePath = FileUtil.createFilePath(domain);
        File file = new File(filePath + fileName);
        multipartFile.transferTo(file);
        amazonS3.putObject(new PutObjectRequest(bucket, file.getPath(), file));
        return filePath + fileName;
    }

    @Override
    public void delete(String filePath) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, filePath));
    }
}
