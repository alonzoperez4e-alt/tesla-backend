package com.tesla.teslabackend.lesson.storage.controller;

import com.tesla.teslabackend.lesson.storage.service.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
@CrossOrigin(origins = "*")
public class StorageController {

    @Autowired
    private S3StorageService s3StorageService;

    // Endpoint para solicitar permisos de subida
    @GetMapping("/presigned-url")
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String folder,
            @RequestParam String filename,
            @RequestParam String contentType) {

        return ResponseEntity.ok(s3StorageService.generatePresignedUploadUrl(folder, filename, contentType));
    }
}
