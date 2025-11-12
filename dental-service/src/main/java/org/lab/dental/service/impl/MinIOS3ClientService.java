package org.lab.dental.service.impl;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.lab.dental.service.S3ClientService;
import org.lab.exception.ApplicationCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class MinIOS3ClientService implements S3ClientService {

    private final MinioClient minioClient;
    private final String bucket;
    private final int durationMinutes;


    @Autowired
    public MinIOS3ClientService(MinioClient minioClient,
                                @Value("${project.variables.minio.bucket}") String bucket,
                                @Value("${project.variables.minio.duration-minutes}") Integer durationMinutes) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.durationMinutes = durationMinutes;
    }


    @Override
    public void uploadPhoto(MultipartFile file, String filename) {
        try (InputStream inputStream = file.getInputStream()) {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(filename)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (MinioException | IOException | GeneralSecurityException e) {
            throw new ApplicationCustomException(e);
        }
    }

    @Override
    public String getFileUrl(String filename) {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(filename)
                .expiry(durationMinutes, TimeUnit.MINUTES)
                .build();
        try {
            return minioClient.getPresignedObjectUrl(args);
        } catch (MinioException | IOException | GeneralSecurityException e) {
            throw new ApplicationCustomException(e);
        }
    }

    @Override
    public InputStream download(String filename) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .build()
            );
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new ApplicationCustomException(e);
        }
    }

    @Override
    public void deletePhoto(String filename) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(filename)
                    .build());
        } catch (MinioException | IOException | GeneralSecurityException e) {
            throw new ApplicationCustomException(e);
        }
    }
}
