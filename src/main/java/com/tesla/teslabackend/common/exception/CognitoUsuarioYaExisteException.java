package com.tesla.teslabackend.common.exception;

public class CognitoUsuarioYaExisteException extends RuntimeException {

    public CognitoUsuarioYaExisteException(String message) {
        super(message);
    }
}
