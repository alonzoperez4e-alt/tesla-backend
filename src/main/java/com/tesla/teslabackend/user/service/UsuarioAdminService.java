package com.tesla.teslabackend.user.service;

import com.tesla.teslabackend.common.exception.CodigoUsuarioYaExisteException;
import com.tesla.teslabackend.common.exception.CognitoNoDisponibleException;
import com.tesla.teslabackend.common.exception.RolNoPermitidoException;
import com.tesla.teslabackend.common.exception.UsuarioNoRegistradoException;
import com.tesla.teslabackend.user.cognito.service.CognitoService;
import com.tesla.teslabackend.user.dto.CrearUsuarioRequest;
import com.tesla.teslabackend.user.dto.ReintentarCognitoRequest;
import com.tesla.teslabackend.user.dto.UsuarioDTO;
import com.tesla.teslabackend.user.entity.Rol;
import com.tesla.teslabackend.user.entity.Usuario;
import com.tesla.teslabackend.user.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orquesta la creación de cuentas de alumno/administrador: BD primero (fuente de
 * verdad), luego Cognito. Si Cognito falla de forma transitoria, la fila en BD
 * queda "pendiente" ({@code cognitoSub == null}) para que el admin pueda
 * reintentar solo el paso de Cognito más tarde, sin duplicar el registro.
 */
@Service
public class UsuarioAdminService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioAdminService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CognitoService cognitoService;

    @Transactional
    public UsuarioDTO crearUsuario(CrearUsuarioRequest dto) {
        validarRolPermitido(dto.rol());

        if (usuarioRepository.existsByCodigoUsuario(dto.codigoUsuario())) {
            throw new CodigoUsuarioYaExisteException(
                    "Ya existe un usuario con el código: " + dto.codigoUsuario());
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .codigoUsuario(dto.codigoUsuario())
                .nombre(dto.nombre())
                .apellido(dto.apellido())
                .rol(dto.rol())
                .area(dto.area())
                .tipoAlumno(dto.tipoAlumno())
                .cognitoSub(null)
                .build());

        return vincularConCognito(usuario, dto.email(), dto.password());
    }

    @Transactional
    public UsuarioDTO reintentarCognito(Integer idUsuario, ReintentarCognitoRequest dto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoRegistradoException(
                        "No existe un usuario con id: " + idUsuario));

        if (usuario.getCognitoSub() != null) {
            return toDto(usuario); // ya vinculado, reintento es no-op idempotente
        }

        return vincularConCognito(usuario, dto.email(), dto.password());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios(boolean soloPendientes) {
        List<Usuario> usuarios = soloPendientes
                ? usuarioRepository.findByCognitoSubIsNullOrderByFechaRegistroAsc()
                : usuarioRepository.findAll();
        return usuarios.stream().map(this::toDto).toList();
    }

    private UsuarioDTO vincularConCognito(Usuario usuario, String email, String password) {
        String username = usuario.getCodigoUsuario();
        try {
            String sub = cognitoService.crearUsuarioCognito(
                    username, email, usuario.getNombre(), usuario.getApellido());

            try {
                cognitoService.establecerPasswordPermanente(username, password);
                cognitoService.agregarUsuarioAGrupo(username, usuario.getRol().name());
            } catch (RuntimeException ex) {
                // AdminCreateUser tuvo éxito pero un paso posterior falló: limpiar
                // para que un reintento posterior no choque con UsernameExistsException.
                cognitoService.eliminarUsuarioCognito(username);
                throw ex;
            }

            usuario.setCognitoSub(sub);
            usuarioRepository.save(usuario);
        } catch (CognitoNoDisponibleException ex) {
            logger.warn("Cognito no disponible al vincular al usuario [{}], queda pendiente de reintento",
                    username, ex);
        }
        return toDto(usuario);
    }

    private void validarRolPermitido(Rol rol) {
        if (rol != Rol.alumno && rol != Rol.administrador) {
            throw new RolNoPermitidoException(
                    "Solo se pueden crear cuentas con rol alumno o administrador");
        }
    }

    private UsuarioDTO toDto(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getCodigoUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getArea(),
                usuario.getTipoAlumno(),
                usuario.getFechaRegistro(),
                usuario.getCognitoSub(),
                usuario.getCognitoSub() == null
        );
    }
}
