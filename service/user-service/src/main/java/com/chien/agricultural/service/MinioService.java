package com.chien.agricultural.service;

import com.chien.agricultural.exception.AppException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    // Hàm upload file và trả về URL
    public String uploadFile(MultipartFile file) {
        try {
            // 1. Kiểm tra bucket có tồn tại không, nếu không thì tạo
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                // Set policy public để ai cũng xem được ảnh (Optional - tùy bảo mật)
                // Hoặc dùng Presigned URL
            }

            // 2. Tạo tên file độc nhất để không bị trùng
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 3. Upload lên MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // 4. Sinh đường dẫn truy cập (Presigned URL) hoặc đường dẫn công khai
            // Cách đơn giản nhất cho đồ án: Trả về tên file, Frontend sẽ ghép với URL MinIO
            // Hoặc trả về Presigned URL có hạn 7 ngày
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7, TimeUnit.DAYS)
                            .build());

        } catch (Exception e) {
            log.error("Lỗi upload file lên MinIO: {}", e.getMessage());
            throw new AppException("Lỗi upload ảnh: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}