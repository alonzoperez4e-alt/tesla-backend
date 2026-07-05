package com.tesla.teslabackend.common.exception;

/**
 * Señala un fallo transitorio al comunicarse con AWS Cognito (servicio caído,
 * timeout, throttling, credenciales/rol IAM sin permisos, etc.), distinto de un
 * error de validación del usuario.
 */
public class CognitoNoDisponibleException extends RuntimeException {

    public CognitoNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
