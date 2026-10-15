package com.ofix.dto;

public class AuthResponse {

    private String token;
    private String tipo;
    private String rol;

    public AuthResponse(String token, String tipo, String rol) {
        this.token = token;
        this.tipo = tipo;
        this.rol = rol;
    }

    public String getToken() { return token; }
    public String getTipo() { return tipo; }
    public String getRol() { return rol; }
}