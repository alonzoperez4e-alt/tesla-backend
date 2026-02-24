package com.tesla.teslabackend.security.infrastructure;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio transversal de infraestructura para interactuar con Cloudinary.
 * Ubicado en 'infrastructure/cloudinary' ya que es un servicio externo
 * que puede ser consumido por cualquier modulo (Preguntas, Usuarios, Materiales).
 */
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Sube un archivo a Cloudinary y retorna la URL segura.
     * * @param multipartFile Archivo recibido desde el frontend
     * @param folder Carpeta de destino en Cloudinary (ej. "academia_tesla/preguntas")
     * @return URL de la imagen subida
     * @throws IOException Si ocurre un error al procesar el archivo
     */
    public String upload(MultipartFile multipartFile, String folder) throws IOException {
        // 1. Convertir MultipartFile a File
        File file = convert(multipartFile);

        // 2. Subir a Cloudinary definiendo la carpeta dinámicamente según el módulo que lo llame
        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                "folder", folder
        ));

        // 3. Limpiar el archivo temporal del servidor local
        if (!file.delete()) {
            System.err.println("Advertencia: No se pudo eliminar el archivo temporal: " + file.getName());
        }

        // 4. Retornar la URL segura (https)
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Elimina una imagen de Cloudinary basándose en su ID público.
     * Útil para cuando el administrador edita o elimina una pregunta.
     * * @param publicId ID público de la imagen en Cloudinary
     */
    public Map delete(String publicId) throws IOException {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    /**
     * Utilidad para convertir MultipartFile a File nativo de Java.
     */
    private File convert(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }
}