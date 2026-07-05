package com.tesla.teslabackend.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UsuarioNoRegistradoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNoRegistrado(UsuarioNoRegistradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("USUARIO_NO_REGISTRADO", ex.getMessage()));
    }

    @ExceptionHandler(CodigoUsuarioYaExisteException.class)
    public ResponseEntity<ErrorResponse> handleCodigoUsuarioYaExiste(CodigoUsuarioYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CODIGO_YA_EXISTE", ex.getMessage()));
    }

    @ExceptionHandler(CognitoUsuarioYaExisteException.class)
    public ResponseEntity<ErrorResponse> handleCognitoUsuarioYaExiste(CognitoUsuarioYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("COGNITO_USUARIO_YA_EXISTE", ex.getMessage()));
    }

    @ExceptionHandler(CognitoNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleCognitoNoDisponible(CognitoNoDisponibleException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("COGNITO_NO_DISPONIBLE", ex.getMessage()));
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    public ResponseEntity<ErrorResponse> handleRolNoPermitido(RolNoPermitidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("ROL_NO_PERMITIDO", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Error no manejado explícitamente (genérico o de validación)", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Ha ocurrido un error inesperado"));
    }
}
