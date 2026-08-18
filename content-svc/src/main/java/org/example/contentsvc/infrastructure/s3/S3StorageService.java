package org.example.contentsvc.infrastructure.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.contentsvc.infrastructure.properties.S3Properties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @PostConstruct
    void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(s3Properties.bucket()).build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                s3Client.createBucket(
                        CreateBucketRequest.builder()
                                .bucket(s3Properties.bucket())
                                .build()
                );
                return;
            }
            throw exception;
        }
    }

    public StoredObject upload(String key, String fileName, String contentType, byte[] content) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .contentType(contentType)
                .contentDisposition("inline; filename=\"" + fileName + "\"")
                .contentLength((long) content.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(content));

        return new StoredObject(s3Properties.bucket(), key, contentType, content.length);
    }

    public PresignedDownloadUrl createDownloadUrl(String key) {
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(s3Properties.downloadUrlTtl());

        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(s3Properties.downloadUrlTtl())
                .getObjectRequest(builder ->
                        builder.bucket(s3Properties.bucket()).key(key)
                ).build();

        String url = s3Presigner.presignGetObject(request).url().toString();

        return new PresignedDownloadUrl(url, expiresAt);
    }

    public record StoredObject(String bucket, String key, String contentType, long sizeBytes) {
    }

    public record PresignedDownloadUrl(String url, OffsetDateTime expiresAt) {
    }
}
