package com.tesla.teslabackend.service;

import com.tesla.teslabackend.dto.login.AuthResponse; // Asegúrate de importar tus DTOs
import com.tesla.teslabackend.dto.login.LoginRequest;
import com.tesla.teslabackend.dto.login.RegisterRequest;
import com.tesla.teslabackend.entity.Rol;
import com.tesla.teslabackend.entity.Usuario;
import com.tesla.teslabackend.repository.UsuarioRepository;
import com.tesla.teslabackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // --- LOGIN ---
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCodigo(), request.getPassword())
        );
        var user = usuarioRepository.findByCodigoUsuario(request.getCodigo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .nombre(user.getNombre())
                .rol(user.getRol().name())
                .build();
    }

    // --- REGISTRO MANUAL (Actualizado con tipoAlumno) ---
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByCodigoUsuario(request.getCodigo())) {
            throw new RuntimeException("El código " + request.getCodigo() + " ya existe.");
        }

        Rol rolUsuario;
        try {
            // Convierte "ALUMNO" o "Alumno" -> "alumno" (valor del Enum)
            rolUsuario = Rol.valueOf(request.getRol().toLowerCase().trim());
        } catch (Exception e) {
            rolUsuario = Rol.alumno;
        }

        var user = Usuario.builder()
                .codigoUsuario(request.getCodigo())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(rolUsuario)
                .area(request.getArea())
                .tipoAlumno(request.getTipoAlumno())
                .build();

        usuarioRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .nombre(user.getNombre())
                .rol(user.getRol().name())
                .build();
    }

    // IMPORTACIÓN EXCEL
    public String importarUsuariosDesdeExcel(MultipartFile file) throws IOException {
        List<Usuario> usuariosAInsertar = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Iterar filas (saltando cabecera i=0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Col 0: Código
                String codigo = dataFormatter.formatCellValue(row.getCell(0));
                if (usuarioRepository.existsByCodigoUsuario(codigo)) continue;

                // Lectura de celdas
                String nombre = row.getCell(1).getStringCellValue();
                String apellido = row.getCell(2).getStringCellValue();
                String rawPass = dataFormatter.formatCellValue(row.getCell(3));

                // Col 4: Rol (Manejo de minúsculas para coincidir con Enum)
                String rolStr = row.getCell(4).getStringCellValue().toLowerCase().trim();

                // Col 5: Área
                String areaStr = null;
                if (row.getCell(5) != null) {
                    areaStr = row.getCell(5).getStringCellValue();
                }

                // Col 6: Tipo Alumno (NUEVO)
                // Leemos la celda G. Si está vacía o no existe, se guarda como null.
                String tipoAlumnoStr = null;
                Cell tipoCell = row.getCell(6);
                if (tipoCell != null && tipoCell.getCellType() == CellType.STRING) {
                    tipoAlumnoStr = tipoCell.getStringCellValue();
                }

                Rol rolEnum;
                try {
                    rolEnum = Rol.valueOf(rolStr);
                } catch (Exception e) {
                    rolEnum = Rol.alumno; // Fallback
                }

                Usuario usuario = Usuario.builder()
                        .codigoUsuario(codigo)
                        .nombre(nombre)
                        .apellido(apellido)
                        .password(passwordEncoder.encode(rawPass))
                        .rol(rolEnum)
                        .area(areaStr)
                        .tipoAlumno(tipoAlumnoStr) // Asignamos el valor leído
                        .build();

                usuariosAInsertar.add(usuario);
            }

            usuarioRepository.saveAll(usuariosAInsertar);
            return "Carga exitosa. " + usuariosAInsertar.size() + " usuarios registrados.";
        }
    }
}