package com.ofix.service;

import com.ofix.dto.ServicioRequest;
import com.ofix.dto.ServicioResponse;
import com.ofix.entity.CategoriaServicio;
import com.ofix.entity.Oferta;
import com.ofix.entity.Servicio;
import com.ofix.entity.Usuario;
import com.ofix.exception.ReglaNegocioException;
import com.ofix.repository.CategoriaServicioRepository;
import com.ofix.repository.OfertaRepository;
import com.ofix.repository.ServicioRepository;
import com.ofix.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ofix.entity.Rol;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaServicioRepository categoriaRepository;
    private final OfertaRepository ofertaRepository;

    public ServicioService(
            ServicioRepository servicioRepository,
            UsuarioRepository usuarioRepository,
            CategoriaServicioRepository categoriaRepository,
            OfertaRepository ofertaRepository) {

        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.ofertaRepository = ofertaRepository;
    }

    public List<ServicioResponse> listarServicios() {
        return servicioRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public List<ServicioResponse> listarServiciosPorCliente(Long clienteId) {
        return servicioRepository.findByClienteId(clienteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Solicitudes pendientes de las categorías del profesional.
    @Transactional(readOnly = true)
    public List<ServicioResponse> listarServiciosDisponibles(
            Long trabajadorId) {

        Usuario trabajador = usuarioRepository.findById(trabajadorId)
                .orElseThrow(() ->
                        new ReglaNegocioException("El usuario no existe"));

        if (trabajador.getCategorias().isEmpty()) {
            return List.of();
        }

        return servicioRepository
                .findByEstadoAndTrabajadorIsNullAndCategoriaInOrderByFechaSolicitudDesc(
                        "PENDIENTE",
                        trabajador.getCategorias())
                .stream()
                .map(servicio -> {
                    ServicioResponse response = convertirAResponse(servicio);
                    response.setYaOfertado(
                            ofertaRepository
                                    .existsByServicioIdAndTrabajadorIdAndEstado(
                                            servicio.getId(),
                                            trabajadorId,
                                            "PENDIENTE"));
                    return response;
                })
                .toList();
    }

    public List<ServicioResponse> listarServiciosPorTrabajador(
            Long trabajadorId) {

        return servicioRepository
                .findByTrabajadorIdAndEstado(
                        trabajadorId,
                        "EN_PROCESO"
                )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Devuelve la solicitud solo si el usuario tiene relación con ella.
    @Transactional(readOnly = true)
    public Optional<ServicioResponse> buscarPorId(Long id, Long usuarioId) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findById(usuarioId);

        if (usuarioOptional.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOptional.get();

        return servicioRepository.findById(id)
                .filter(servicio -> puedeVer(servicio, usuario))
                .map(this::convertirAResponse);
    }

    private boolean puedeVer(Servicio servicio, Usuario usuario) {

        if (usuario.getRol() == Rol.ADMIN) {
            return true;
        }

        if (servicio.getCliente().getId().equals(usuario.getId())) {
            return true;
        }

        if (servicio.getTrabajador() != null
                && servicio.getTrabajador().getId().equals(usuario.getId())) {
            return true;
        }

        // Un profesional puede ver las solicitudes pendientes de sus categorías.
        return usuario.getRol() == Rol.TRABAJADOR
                && "PENDIENTE".equals(servicio.getEstado())
                && usuario.getCategorias().stream().anyMatch(
                        c -> c.getId().equals(servicio.getCategoria().getId()));
    }

    public Optional<ServicioResponse> guardarServicio(
            ServicioRequest request,
            Long clienteId) {

        Optional<Usuario> cliente =
                usuarioRepository.findById(clienteId);

        Optional<CategoriaServicio> categoria =
                categoriaRepository.findById(request.getCategoriaId());

        if (cliente.isEmpty() || categoria.isEmpty()) {
            return Optional.empty();
        }

        if (cliente.get().getRol() != com.ofix.entity.Rol.CLIENTE) {
            throw new ReglaNegocioException(
                    "Solo los clientes pueden crear servicios"
            );
        }

        Servicio servicio = new Servicio();

        servicio.setTitulo(request.getTitulo());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDireccion(request.getDireccion());
        servicio.setCliente(cliente.get());
        servicio.setCategoria(categoria.get());

        aplicarDatosOpcionales(servicio, request);

        Servicio servicioGuardado =
                servicioRepository.save(servicio);

        return Optional.of(
                convertirAResponse(servicioGuardado)
        );
    }

    public Optional<ServicioResponse> actualizarServicio(
            Long id,
            ServicioRequest request,
            Long usuarioId) {

        return servicioRepository.findById(id).map(servicio -> {

            if (!servicio.getCliente().getId().equals(usuarioId)) {
                throw new ReglaNegocioException(
                        "Solo el cliente que creó el servicio puede modificarlo"
                );
            }

            if (!servicio.getEstado().equals("PENDIENTE")) {
                throw new ReglaNegocioException(
                        "Solo se pueden modificar servicios pendientes"
                );
            }

            servicio.setTitulo(request.getTitulo());
            servicio.setDescripcion(request.getDescripcion());
            servicio.setDireccion(request.getDireccion());

            aplicarDatosOpcionales(servicio, request);

            return convertirAResponse(
                    servicioRepository.save(servicio)
            );
        });
    }

    @Transactional
    public void eliminarServicio(
            Long id,
            Long usuarioId) {

        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() ->
                        new ReglaNegocioException(
                                "El servicio no existe"
                        )
                );

        if (!servicio.getCliente().getId().equals(usuarioId)) {
            throw new ReglaNegocioException(
                    "Solo el cliente que creó el servicio puede eliminarlo"
            );
        }

        if (!servicio.getEstado().equals("PENDIENTE")) {
            throw new ReglaNegocioException(
                    "Solo se pueden eliminar servicios pendientes"
            );
        }

        // Se eliminan primero las ofertas asociadas.
        ofertaRepository.deleteAll(ofertaRepository.findByServicioId(id));

        servicioRepository.delete(servicio);
    }

    
    public Optional<ServicioResponse> finalizarServicio(
            Long servicioId,
            Long trabajadorId) {

        return servicioRepository.findById(servicioId)
                .map(servicio -> {

                    if (!servicio.getEstado().equals("EN_PROCESO")) {
                        throw new ReglaNegocioException(
                                "El servicio debe estar en proceso para poder finalizarlo"
                        );
                    }

                    if (servicio.getTrabajador() == null ||
                            !servicio.getTrabajador()
                                    .getId()
                                    .equals(trabajadorId)) {

                        throw new ReglaNegocioException(
                                "Solo el trabajador asignado puede finalizar este servicio"
                        );
                    }

                    servicio.setEstado("COMPLETADO");

                    Servicio servicioActualizado =
                            servicioRepository.save(servicio);

                    return convertirAResponse(
                            servicioActualizado
                    );
                });
    }

    @Transactional
    public Optional<ServicioResponse> cancelarServicio(
            Long servicioId,
            Long clienteId) {

        return servicioRepository.findById(servicioId)
                .map(servicio -> {

                    if (!servicio.getEstado().equals("PENDIENTE")) {
                        throw new ReglaNegocioException(
                                "Solo se pueden cancelar servicios pendientes"
                        );
                    }

                    if (!servicio.getCliente()
                            .getId()
                            .equals(clienteId)) {

                        throw new ReglaNegocioException(
                                "Solo el cliente que creó el servicio puede cancelarlo"
                        );
                    }

                    servicio.setEstado("CANCELADO");

                    // Las ofertas pendientes dejan de tener sentido.
                    List<Oferta> pendientes =
                            ofertaRepository.findByServicioIdAndEstado(
                                    servicio.getId(), "PENDIENTE");
                    pendientes.forEach(o -> o.setEstado("RECHAZADA"));
                    ofertaRepository.saveAll(pendientes);

                    Servicio servicioActualizado =
                            servicioRepository.save(servicio);

                    return convertirAResponse(
                            servicioActualizado
                    );
                });
    }

    // Valida y aplica los datos opcionales de la solicitud.
    private void aplicarDatosOpcionales(
            Servicio servicio,
            ServicioRequest request) {

        if (request.getFechaProgramada() != null
                && request.getFechaProgramada().isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException(
                    "La fecha del servicio no puede estar en el pasado");
        }

        if (request.getPresupuesto() != null
                && request.getPresupuesto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException(
                    "El presupuesto debe ser mayor que cero");
        }

        servicio.setFechaProgramada(request.getFechaProgramada());
        servicio.setPresupuesto(request.getPresupuesto());
    }

    private ServicioResponse convertirAResponse(
            Servicio servicio) {

        ServicioResponse response =
                new ServicioResponse();

        response.setId(servicio.getId());
        response.setTitulo(servicio.getTitulo());
        response.setDescripcion(servicio.getDescripcion());
        response.setDireccion(servicio.getDireccion());
        response.setFechaSolicitud(servicio.getFechaSolicitud());
        response.setFechaProgramada(servicio.getFechaProgramada());
        response.setPresupuesto(servicio.getPresupuesto());
        response.setPrecioAcordado(servicio.getPrecioAcordado());
        response.setEstado(servicio.getEstado());

        response.setTotalOfertas(
                ofertaRepository.countByServicioIdAndEstado(
                        servicio.getId(), "PENDIENTE")
        );

        response.setClienteId(
                servicio.getCliente().getId()
        );

        response.setClienteNombre(
                servicio.getCliente().getNombre()
                        + " "
                        + servicio.getCliente().getApellido()
        );

        if (servicio.getTrabajador() != null) {

            response.setTrabajadorId(
                    servicio.getTrabajador().getId()
            );

            response.setTrabajadorNombre(
                    servicio.getTrabajador().getNombre()
                            + " "
                            + servicio.getTrabajador().getApellido()
            );
        }

        response.setCategoriaId(
                servicio.getCategoria().getId()
        );

        response.setCategoriaNombre(
                servicio.getCategoria().getNombre()
        );

        return response;
    }
}