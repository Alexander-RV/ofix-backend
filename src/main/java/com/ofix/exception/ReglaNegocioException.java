package com.ofix.exception;

// Representa un error cuando no se cumple una regla del negocio.
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}