package com.ofix.service;


import com.ofix.dto.UsuarioRequest;
import com.ofix.dto.UsuarioResponse;
import com.ofix.entity.Rol;
import com.ofix.entity.Usuario;
import com.ofix.exception.ReglaNegocioException;
import com.ofix.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import com.ofix.dto.ActualizarPerfilRequest;
import com.ofix.dto.CambiarPasswordRequest;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        
    }

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public Optional<UsuarioResponse> buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::convertirAResponse);
    }

    public UsuarioResponse guardarUsuario(UsuarioRequest request) {

        // Verificamos si el email ya está registrado. 

        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ReglaNegocioException(
                "El email ya está registrado"
            );
        }

        // Convertimos los datos recibidos en una entidad.
        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());

        // La contraseña se almacena utilizando Bcrypt
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        //Todo registro público comienza como cliente
        usuario.setRol(Rol.CLIENTE);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Devolvemos los datos sin incluir la contraseña.
        return convertirAResponse(usuarioGuardado);
    }

    public Optional<UsuarioResponse> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(this::convertirAResponse);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }


    public Optional<UsuarioResponse> actualizarUsuario(
            Long id,
            UsuarioRequest request,
            Long usuarioAutenticado) {

        return usuarioRepository.findById(id).map(usuario -> {

            // Solo el usuario autenticado puede actualizar su propio perfil. 
            if (!usuario.getId().equals(usuarioAutenticado)) {
                throw new ReglaNegocioException("Solo puedes modificar tu propio perfil");
            }

            Optional<Usuario> usuarioConEmail =
                    usuarioRepository.findByEmail(request.getEmail());

            // El email solo puede pertenecer al mismo usuario
            if (usuarioConEmail.isPresent()
                    && !usuarioConEmail.get().getId().equals(id)) {
                
                throw new ReglaNegocioException(
                    "El email ya está registrado por otro usuario");
                
            }

            usuario.setNombre(request.getNombre());
            usuario.setApellido(request.getApellido());
            usuario.setEmail(request.getEmail());

            //Encriptación del password antes de guardarla
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
            

            return convertirAResponse(usuarioRepository.save(usuario));
        });
    }

    public Optional<UsuarioResponse> actualizarPerfil(
            Long usuarioId,
            ActualizarPerfilRequest request) {

        return usuarioRepository.findById(usuarioId)
                .map(usuario -> {

                    Optional<Usuario> usuarioConEmail =
                            usuarioRepository.findByEmail(request.getEmail());

                    if (usuarioConEmail.isPresent()
                            && !usuarioConEmail.get()
                                    .getId()
                                    .equals(usuarioId)) {

                        throw new ReglaNegocioException(
                                "El email ya está registrado por otro usuario"
                        );
                    }

                    usuario.setNombre(request.getNombre());
                    usuario.setApellido(request.getApellido());
                    usuario.setEmail(request.getEmail());

                    return convertirAResponse(
                            usuarioRepository.save(usuario)
                    );
                });
    }

    //Confirmación y cambio de password
    public void cambiarPassword(
            Long usuarioId,
            CambiarPasswordRequest request) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ReglaNegocioException(
                                "El usuario no existe"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPasswordActual(),
                usuario.getPassword())) {

            throw new ReglaNegocioException(
                    "La contraseña actual es incorrecta"
            );
        }

        if (!request.getNuevaPassword().equals(
                request.getConfirmarPassword())) {

            throw new ReglaNegocioException(
                    "La nueva contraseña y su confirmación no coinciden"
            );
        }

        if (passwordEncoder.matches(
                request.getNuevaPassword(),
                usuario.getPassword())) {

            throw new ReglaNegocioException(
                    "La nueva contraseña debe ser diferente a la actual"
            );
        }

        usuario.setPassword(
                passwordEncoder.encode(
                        request.getNuevaPassword()
                )
        );

        usuarioRepository.save(usuario);
    }

    public Optional<UsuarioResponse> cambiarEstado(Long id, boolean activo) {
        return usuarioRepository.findById(id).map(usuario -> {
                usuario.setActivo(activo);
                return convertirAResponse(usuarioRepository.save(usuario));
        });
    }


    // Convierte la entidad Usuario en una respuesta segura para la API.
    private UsuarioResponse convertirAResponse(Usuario usuario) {

        UsuarioResponse response = new UsuarioResponse();

        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setActivo(usuario.getActivo());
        response.setFechaRegistro(usuario.getFechaRegistro());

        return response;
    }
}