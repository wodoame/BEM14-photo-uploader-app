package com.labs.photouploader.service;

import com.labs.photouploader.model.Photo;
import com.labs.photouploader.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    public PhotoService(PhotoRepository photoRepository, S3Client s3Client) {
        this.photoRepository = photoRepository;
        this.s3Client = s3Client;
    }

    public void upload(MultipartFile file, String description) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase()
                : "jpg";

        String s3Key = "photos/" + UUID.randomUUID() + "." + ext;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(s3Key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        Photo photo = new Photo();
        photo.setFilename(originalFilename);
        photo.setDescription(description);
        photo.setS3Key(s3Key);
        photoRepository.save(photo);
    }

    public List<Photo> getAll() {
        return photoRepository.findAllByOrderByCreatedAtDesc();
    }

    public void delete(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found: " + id));
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(photo.getS3Key())
                .build());
        photoRepository.delete(photo);
    }

    public String getCloudFrontDomain() {
        return cloudFrontDomain;
    }
}
