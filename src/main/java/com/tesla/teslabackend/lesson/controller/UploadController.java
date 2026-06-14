package com.tesla.teslabackend.lesson.controller; // Ajusta el paquete según donde lo guardes

import com.tesla.teslabackend.security.infrastructure.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*") // Ajusta esto según tu configuración de CORS
public class UploadController {

    private final S3Service s3Service;

    @Autowired
    public UploadController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // "uploads" es el nombre de la carpeta dentro de tu bucket S3
            String fileUrl = s3Service.uploadFile(file, "uploads");

            // Retornamos un JSON estructurado con la URL { "url": "https://media..." }
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al subir el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}