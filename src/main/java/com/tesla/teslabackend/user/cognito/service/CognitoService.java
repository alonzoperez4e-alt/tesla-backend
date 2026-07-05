package com.tesla.teslabackend.user.cognito.service;

import com.tesla.teslabackend.common.exception.CognitoNoDisponibleException;
import com.tesla.teslabackend.common.exception.CognitoUsuarioYaExisteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

/**
 * Encapsula las operaciones de administración de Cognito (Admin*) necesarias para
 * crear cuentas de alumno/administrador desde el dashboard. Todas las llamadas de
 * error transitorio (servicio caído, timeout, credenciales/rol IAM sin permisos)
 * se traducen a {@link CognitoNoDisponibleException} para que el flujo de creación
 * pueda dejar la cuenta en estado "pendiente" en vez de fallar por completo.
 */
@Service
public class CognitoService {

    private static final Logger logger = LoggerFactory.getLogger(CognitoService.class);

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    /**
     * Crea el usuario en el User Pool (email verificado, sin enviar el correo de
     * bienvenida automático de Cognito) y devuelve su {@code sub}.
     */
    public String crearUsuarioCognito(String username, String email, String nombre, String apellido) {
        try {
            AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build(),
                            AttributeType.builder().name("given_name").value(nombre).build(),
                            AttributeType.builder().name("family_name").value(apellido).build()
                    )
                    .messageAction(MessageActionType.SUPPRESS)
                    .build();

            AdminCreateUserResponse response = cognitoClient.adminCreateUser(request);

            return response.user().attributes().stream()
                    .filter(attr -> "sub".equals(attr.name()))
                    .map(AttributeType::value)
                    .findFirst()
                    .orElseThrow(() -> new CognitoNoDisponibleException(
                            "Cognito no devolvió el atributo 'sub' del usuario creado", null));
        } catch (UsernameExistsException ex) {
            throw new CognitoUsuarioYaExisteException(
                    "Ya existe un usuario en Cognito con ese identificador/email: " + username);
        } catch (SdkException ex) {
            logger.warn("Fallo transitorio al crear usuario en Cognito [{}]", username, ex);
            throw new CognitoNoDisponibleException("No se pudo crear el usuario en Cognito", ex);
        }
    }

    /**
     * Recupera el {@code sub} de un usuario que ya existe en Cognito (p. ej. cuando
     * {@link #crearUsuarioCognito} lanzó {@link CognitoUsuarioYaExisteException}), para
     * poder vincularlo en BD sin intentar crearlo de nuevo.
     */
    public String obtenerSubUsuarioCognito(String username) {
        try {
            AdminGetUserResponse response = cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build());

            return response.userAttributes().stream()
                    .filter(attr -> "sub".equals(attr.name()))
                    .map(AttributeType::value)
                    .findFirst()
                    .orElseThrow(() -> new CognitoNoDisponibleException(
                            "Cognito no devolvió el atributo 'sub' del usuario existente", null));
        } catch (SdkException ex) {
            logger.warn("Fallo transitorio al recuperar el usuario existente en Cognito [{}]", username, ex);
            throw new CognitoNoDisponibleException("No se pudo recuperar el usuario existente en Cognito", ex);
        }
    }

    /** Fija la contraseña como permanente (sin forzar cambio en el primer login). */
    public void establecerPasswordPermanente(String username, String password) {
        try {
            cognitoClient.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .password(password)
                    .permanent(true)
                    .build());
        } catch (SdkException ex) {
            logger.warn("Fallo transitorio al fijar password en Cognito [{}]", username, ex);
            throw new CognitoNoDisponibleException("No se pudo fijar la contraseña en Cognito", ex);
        }
    }

    /** Agrega el usuario al grupo Cognito correspondiente a su rol ("administrador" o "alumno"). */
    public void agregarUsuarioAGrupo(String username, String grupo) {
        try {
            cognitoClient.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .groupName(grupo)
                    .build());
        } catch (SdkException ex) {
            logger.warn("Fallo transitorio al agregar usuario [{}] al grupo [{}]", username, grupo, ex);
            throw new CognitoNoDisponibleException("No se pudo agregar el usuario al grupo de Cognito", ex);
        }
    }

    /**
     * Elimina un usuario a medio crear en Cognito (p. ej. si {@code AdminCreateUser}
     * tuvo éxito pero un paso posterior falló), para que un reintento posterior no
     * choque con {@link UsernameExistsException}. Los fallos al limpiar solo se
     * loguean: no deben impedir que la fila en BD quede marcada como pendiente.
     */
    public void eliminarUsuarioCognito(String username) {
        try {
            cognitoClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build());
        } catch (SdkException ex) {
            logger.error("No se pudo limpiar el usuario a medio crear en Cognito [{}]", username, ex);
        }
    }
}
