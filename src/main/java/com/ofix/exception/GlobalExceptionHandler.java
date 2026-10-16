package com.ofix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> manejarReglaNegocio(
            ReglaNegocioException ex) {

        return ResponseEntity.badRequest().body(
                Map.of("mensaje", ex.getMessage())
        );
    }

    // Login con correo o contraseña incorrectos.
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> manejarCredencialesInvalidas(
            CredencialesInvalidasException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("mensaje", ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarErroresValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errores.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> respuesta = new HashMap<>();

        respuesta.put("mensaje", "Hay errores de validación");
        respuesta.put("errores", errores);

        return ResponseEntity.badRequest().body(respuesta);
    }
}