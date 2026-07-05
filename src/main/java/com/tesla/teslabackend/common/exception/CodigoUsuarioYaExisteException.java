package com.tesla.teslabackend.common.exception;

public class CodigoUsuarioYaExisteException extends RuntimeException {

    public CodigoUsuarioYaExisteException(String message) {
        super(message);
    }
}
