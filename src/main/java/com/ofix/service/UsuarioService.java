package com.ofix.service;

import com.ofix.dto.ActualizarPerfilRequest;
import com.ofix.dto.CambiarPasswordRequest;
import com.ofix.dto.UsuarioRequest;
import com.ofix.dto.UsuarioResponse;
import com.ofix.entity.CategoriaServicio;
import com.ofix.entity.Rol;
import com.ofix.entity.Usuario;
import com.ofix.exception.ReglaNegocioException;
import com.ofix.repository.CategoriaServicioRepository;
import com.ofix.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoriaServicioRepository categoriaRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            CategoriaServicioRepository categoriaRepository) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoriaRepository = categoriaRepository;
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
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ReglaNegocioException("El email ya está registrado");
        }

        // El registro público solo permite CLIENTE o TRABAJADOR.
        Rol rol = request.getRol() != null ? request.getRol() : Rol.CLIENTE;
        if (rol == Rol.ADMIN) {
            throw new ReglaNegocioException("No es posible registrarse con ese rol");
        }

        // Convertimos los datos recibidos en una entidad.
        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(email);

        // La contraseña se almacena utilizando Bcrypt
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        usuario.setRol(rol);

        // Si es profesional, validamos y guardamos sus datos profesionales.
        if (rol == Rol.TRABAJADOR) {
            aplicarDatosProfesionales(usuario, request);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Devolvemos los datos sin incluir la contraseña.
        return convertirAResponse(usuarioGuardado);
    }

    private void aplicarDatosProfesionales(Usuario usuario, UsuarioRequest request) {

        if (request.getProfesion() == null || request.getProfesion().isBlank()) {
            throw new ReglaNegocioException(
                    "La profesión u oficio es obligatoria para los profesionales");
        }

        Integer anos = request.getAnosExperiencia();
        if (anos == null || anos < 0 || anos > 60) {
            throw new ReglaNegocioException(
                    "Los años de experiencia deben estar entre 0 y 60");
        }

        if (request.getZonaTrabajo() == null || request.getZonaTrabajo().isBlank()) {
            throw new ReglaNegocioException(
                    "La zona de trabajo es obligatoria para los profesionales");
        }

        List<Long> ids = request.getCategoriaIds();
        if (ids == null || ids.isEmpty()) {
            throw new ReglaNegocioException(
                    "Selecciona al menos una categoría de servicio");
        }

        List<CategoriaServicio> categorias = categoriaRepository.findAllById(ids);

        boolean todasValidas =
                categorias.size() == new HashSet<>(ids).size()
                && categorias.stream()
                        .allMatch(c -> Boolean.TRUE.equals(c.getActivo()));

        if (!todasValidas) {
            throw new ReglaNegocioException(
                    "Alguna de las categorías seleccionadas no existe o no está activa");
        }

        usuario.setProfesion(request.getProfesion().trim());
        usuario.setAnosExperiencia(anos);
        usuario.setZonaTrabajo(request.getZonaTrabajo().trim());

        String descripcion = request.getDescripcionProfesional();
        usuario.setDescripcionProfesional(
                (descripcion == null || descripcion.isBlank()) ? null : descripcion.trim());

        usuario.setCategorias(new HashSet<>(categorias));
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

            // Encriptación del password antes de guardarla
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
                                "El email ya está registrado por otro usuario");
                    }

                    usuario.setNombre(request.getNombre());
                    usuario.setApellido(request.getApellido());
                    usuario.setEmail(request.getEmail());

                    return convertirAResponse(usuarioRepository.save(usuario));
                });
    }

    // Confirmación y cambio de password
    public void cambiarPassword(
            Long usuarioId,
            CambiarPasswordRequest request) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ReglaNegocioException("El usuario no existe"));

        if (!passwordEncoder.matches(
                request.getPasswordActual(),
                usuario.getPassword())) {

            throw new ReglaNegocioException("La contraseña actual es incorrecta");
        }

        if (!request.getNuevaPassword().equals(
                request.getConfirmarPassword())) {

            throw new ReglaNegocioException(
                    "La nueva contraseña y su confirmación no coinciden");
        }

        if (passwordEncoder.matches(
                request.getNuevaPassword(),
                usuario.getPassword())) {

            throw new ReglaNegocioException(
                    "La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));

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

        if (usuario.getRol() == Rol.TRABAJADOR) {
            response.setProfesion(usuario.getProfesion());
            response.setAnosExperiencia(usuario.getAnosExperiencia());
            response.setDescripcionProfesional(usuario.getDescripcionProfesional());
            response.setZonaTrabajo(usuario.getZonaTrabajo());
            response.setCategorias(
                    usuario.getCategorias().stream()
                            .map(CategoriaServicio::getNombre)
                            .sorted()
                            .toList());
        }

        return response;
    }
}