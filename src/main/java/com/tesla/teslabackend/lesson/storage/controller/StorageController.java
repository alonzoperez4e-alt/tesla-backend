package com.tesla.teslabackend.lesson.storage.controller;

import com.tesla.teslabackend.lesson.storage.service.S3StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {

    private final S3StorageService s3StorageService;

    public StorageController(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    @GetMapping("/presigned-url")
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String folder,
            @RequestParam String filename,
            @RequestParam String contentType) {

        return ResponseEntity.ok(
                s3StorageService.generatePresignedUploadUrl(folder, filename, contentType)
        );
    }
}