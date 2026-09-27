package com.ofix.service;

import com.ofix.dto.ServicioRequest;
import com.ofix.dto.ServicioResponse;
import com.ofix.entity.CategoriaServicio;
import com.ofix.entity.Servicio;
import com.ofix.entity.Usuario;
import com.ofix.exception.ReglaNegocioException;
import com.ofix.repository.CategoriaServicioRepository;
import com.ofix.repository.ServicioRepository;
import com.ofix.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaServicioRepository categoriaRepository;

    public ServicioService(
            ServicioRepository servicioRepository,
            UsuarioRepository usuarioRepository,
            CategoriaServicioRepository categoriaRepository) {

        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
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

    public List<ServicioResponse> listarServiciosDisponibles() {
        return servicioRepository
                .findByEstadoAndTrabajadorIsNull("PENDIENTE")
                .stream()
                .map(this::convertirAResponse)
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

    public Optional<ServicioResponse> buscarPorId(Long id) {
        return servicioRepository.findById(id)
                .map(this::convertirAResponse);
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

            return convertirAResponse(
                    servicioRepository.save(servicio)
            );
        });
    }

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

        servicioRepository.delete(servicio);
    }

    public Optional<ServicioResponse> aceptarServicio(
            Long servicioId,
            Long trabajadorId) {

        Optional<Servicio> servicioOptional =
                servicioRepository.findById(servicioId);

        Optional<Usuario> trabajadorOptional =
                usuarioRepository.findById(trabajadorId);

        if (servicioOptional.isEmpty() ||
                trabajadorOptional.isEmpty()) {

            return Optional.empty();
        }

        Servicio servicio = servicioOptional.get();
        Usuario trabajador = trabajadorOptional.get();

        if (!trabajador.getRol().name().equals("TRABAJADOR")) {
            throw new ReglaNegocioException(
                    "Solo los trabajadores pueden aceptar servicios"
            );
        }

        if (!servicio.getEstado().equals("PENDIENTE")) {
            throw new ReglaNegocioException(
                    "El servicio no está disponible para ser aceptado"
            );
        }

        servicio.setTrabajador(trabajador);
        servicio.setEstado("EN_PROCESO");

        Servicio servicioActualizado =
                servicioRepository.save(servicio);

        return Optional.of(
                convertirAResponse(servicioActualizado)
        );
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

                    Servicio servicioActualizado =
                            servicioRepository.save(servicio);

                    return convertirAResponse(
                            servicioActualizado
                    );
                });
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
        response.setEstado(servicio.getEstado());

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