package com.tesla.teslabackend.lesson.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class S3StorageService {

    private static final Logger logger =
            LoggerFactory.getLogger(S3StorageService.class);

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.public-base-url}")
    private String publicBaseUrl;

    @Value("${aws.s3.presigned-url-duration-minutes}")
    private long presignedUrlDurationMinutes;

    @Value("${aws.s3.allowed-content-types}")
    private List<String> allowedContentTypes;

    public S3StorageService(
            S3Presigner s3Presigner,
            S3Client s3Client
    ) {
        this.s3Presigner = s3Presigner;
        this.s3Client = s3Client;
    }

    /**
     * Genera una URL prefirmada para subir archivos
     * directamente a S3 mediante HTTP PUT.
     */
    public Map<String, String> generatePresignedUploadUrl(
            String folder,
            String originalFilename,
            String contentType
    ) {

        validateFolder(folder);
        validateContentType(contentType);

        String extension = extractExtension(originalFilename);

        // El prefijo "images/" debe coincidir con el path pattern "/images/*"
        // del comportamiento de CloudFront que enruta al bucket de imagenes.
        String fileKey = String.format(
                "images/%s/%s%s",
                folder,
                UUID.randomUUID(),
                extension
        );

        PutObjectRequest objectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileKey)
                        .contentType(contentType)
                        .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofMinutes(
                                        presignedUrlDurationMinutes
                                )
                        )
                        .putObjectRequest(objectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

        logger.info(
                "URL prefirmada generada para [{}] con content-type [{}]",
                fileKey,
                contentType
        );

        Map<String, String> response = new HashMap<>();

        response.put(
                "presignedUrl",
                presignedRequest.url().toExternalForm()
        );

        response.put(
                "fileKey",
                fileKey
        );

        return response;
    }

    /**
     * Resuelve la URL publica actual (via CDN) para una Object Key de S3.
     * Se calcula en cada lectura para no acoplar datos persistidos a un dominio de CDN puntual.
     */
    public String toPublicUrl(String fileKey) {

        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }

        return publicBaseUrl + "/" + fileKey;
    }

    /**
     * Elimina un archivo de S3.
     */
    public void deleteFile(String fileKey) {

        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileKey)
                        .build();

        s3Client.deleteObject(request);

        logger.info(
                "Archivo eliminado de S3 [{}]",
                fileKey
        );
    }

    private void validateFolder(String folder) {

        if (folder == null || folder.isBlank()) {
            throw new IllegalArgumentException(
                    "El folder no puede ser nulo o vacío."
            );
        }
    }

    private void validateContentType(String contentType) {

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException(
                    "El content type es obligatorio."
            );
        }

        if (!allowedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido: " + contentType
            );
        }
    }

    private String extractExtension(String originalFilename) {

        if (originalFilename == null ||
                !originalFilename.contains(".")) {
            return "";
        }

        return originalFilename.substring(
                originalFilename.lastIndexOf(".")
        );
    }
}