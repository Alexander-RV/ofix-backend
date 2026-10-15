package com.ofix.service;

import com.ofix.dto.OfertaRequest;
import com.ofix.dto.OfertaResponse;
import com.ofix.entity.Oferta;
import com.ofix.entity.Rol;
import com.ofix.entity.Servicio;
import com.ofix.entity.Usuario;
import com.ofix.exception.ReglaNegocioException;
import com.ofix.repository.OfertaRepository;
import com.ofix.repository.ServicioRepository;
import com.ofix.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfertaService {

    private final OfertaRepository ofertaRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    public OfertaService(
            OfertaRepository ofertaRepository,
            ServicioRepository servicioRepository,
            UsuarioRepository usuarioRepository) {

        this.ofertaRepository = ofertaRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // El profesional envía una oferta a una solicitud.
    @Transactional
    public OfertaResponse crearOferta(
            Long servicioId,
            Long trabajadorId,
            OfertaRequest request) {

        Usuario trabajador = usuarioRepository.findById(trabajadorId)
                .orElseThrow(() ->
                        new ReglaNegocioException("El usuario no existe"));

        if (trabajador.getRol() != Rol.TRABAJADOR) {
            throw new ReglaNegocioException(
                    "Solo los profesionales pueden enviar ofertas");
        }

        Servicio servicio = buscarServicio(servicioId);

        if (!"PENDIENTE".equals(servicio.getEstado())) {
            throw new ReglaNegocioException(
                    "La solicitud ya no está disponible para recibir ofertas");
        }

        boolean atiendeCategoria = trabajador.getCategorias().stream()
                .anyMatch(c -> c.getId()
                        .equals(servicio.getCategoria().getId()));

        if (!atiendeCategoria) {
            throw new ReglaNegocioException(
                    "Esta solicitud no pertenece a tus categorías de servicio");
        }

        if (ofertaRepository.existsByServicioIdAndTrabajadorIdAndEstadoIn(
                servicioId, trabajadorId, List.of("PENDIENTE", "ACEPTADA"))) {
            throw new ReglaNegocioException(
                    "Ya enviaste una oferta para esta solicitud");
        }

        Oferta oferta = new Oferta();
        oferta.setServicio(servicio);
        oferta.setTrabajador(trabajador);
        oferta.setPrecio(request.getPrecio());

        String mensaje = request.getMensaje();
        oferta.setMensaje(
                (mensaje == null || mensaje.isBlank()) ? null : mensaje.trim());

        return convertirAResponse(ofertaRepository.save(oferta));
    }

    // El cliente dueño ve las ofertas de su solicitud.
    @Transactional(readOnly = true)
    public List<OfertaResponse> listarOfertasDeServicio(
            Long servicioId,
            Long clienteId) {

        Servicio servicio = buscarServicio(servicioId);
        validarCliente(servicio, clienteId);

        return ofertaRepository
                .findByServicioIdAndEstadoNotOrderByFechaCreacionDesc(
                        servicioId, "RETIRADA")
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // El profesional ve las ofertas que ha enviado.
    @Transactional(readOnly = true)
    public List<OfertaResponse> listarMisOfertas(Long trabajadorId) {
        return ofertaRepository
                .findByTrabajadorIdOrderByFechaCreacionDesc(trabajadorId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // El cliente acepta una oferta: la solicitud queda asignada.
    @Transactional
    public OfertaResponse aceptarOferta(Long ofertaId, Long clienteId) {

        Oferta oferta = buscarOferta(ofertaId);
        Servicio servicio = oferta.getServicio();

        validarCliente(servicio, clienteId);

        if (!"PENDIENTE".equals(oferta.getEstado())) {
            throw new ReglaNegocioException("La oferta ya no está pendiente");
        }

        if (!"PENDIENTE".equals(servicio.getEstado())) {
            throw new ReglaNegocioException(
                    "La solicitud ya no está disponible");
        }

        oferta.setEstado("ACEPTADA");

        servicio.setTrabajador(oferta.getTrabajador());
        servicio.setPrecioAcordado(oferta.getPrecio());
        servicio.setEstado("EN_PROCESO");
        servicioRepository.save(servicio);

        // Las demás ofertas pendientes se rechazan automáticamente.
        List<Oferta> otras = ofertaRepository
                .findByServicioIdAndEstado(servicio.getId(), "PENDIENTE");

        for (Oferta otra : otras) {
            if (!otra.getId().equals(oferta.getId())) {
                otra.setEstado("RECHAZADA");
            }
        }
        ofertaRepository.saveAll(otras);

        return convertirAResponse(ofertaRepository.save(oferta));
    }

    // El cliente rechaza una oferta.
    @Transactional
    public OfertaResponse rechazarOferta(Long ofertaId, Long clienteId) {

        Oferta oferta = buscarOferta(ofertaId);

        validarCliente(oferta.getServicio(), clienteId);

        if (!"PENDIENTE".equals(oferta.getEstado())) {
            throw new ReglaNegocioException("La oferta ya no está pendiente");
        }

        oferta.setEstado("RECHAZADA");

        return convertirAResponse(ofertaRepository.save(oferta));
    }

    // El profesional retira su propia oferta.
    @Transactional
    public OfertaResponse retirarOferta(Long ofertaId, Long trabajadorId) {

        Oferta oferta = buscarOferta(ofertaId);

        if (!oferta.getTrabajador().getId().equals(trabajadorId)) {
            throw new ReglaNegocioException(
                    "Solo puedes retirar tus propias ofertas");
        }

        if (!"PENDIENTE".equals(oferta.getEstado())) {
            throw new ReglaNegocioException(
                    "Solo se pueden retirar ofertas pendientes");
        }

        oferta.setEstado("RETIRADA");

        return convertirAResponse(ofertaRepository.save(oferta));
    }

    private Servicio buscarServicio(Long servicioId) {
        return servicioRepository.findById(servicioId)
                .orElseThrow(() ->
                        new ReglaNegocioException("La solicitud no existe"));
    }

    private Oferta buscarOferta(Long ofertaId) {
        return ofertaRepository.findById(ofertaId)
                .orElseThrow(() ->
                        new ReglaNegocioException("La oferta no existe"));
    }

    private void validarCliente(Servicio servicio, Long clienteId) {
        if (!servicio.getCliente().getId().equals(clienteId)) {
            throw new ReglaNegocioException(
                    "Solo el cliente que creó la solicitud puede hacer esto");
        }
    }

    private OfertaResponse convertirAResponse(Oferta oferta) {

        OfertaResponse response = new OfertaResponse();

        response.setId(oferta.getId());
        response.setPrecio(oferta.getPrecio());
        response.setMensaje(oferta.getMensaje());
        response.setEstado(oferta.getEstado());
        response.setFechaCreacion(oferta.getFechaCreacion());

        Servicio servicio = oferta.getServicio();
        response.setServicioId(servicio.getId());
        response.setServicioTitulo(servicio.getTitulo());
        response.setServicioEstado(servicio.getEstado());
        response.setCategoriaNombre(servicio.getCategoria().getNombre());

        Usuario trabajador = oferta.getTrabajador();
        response.setTrabajadorId(trabajador.getId());
        response.setTrabajadorNombre(
                trabajador.getNombre() + " " + trabajador.getApellido());
        response.setTrabajadorProfesion(trabajador.getProfesion());
        response.setTrabajadorAnosExperiencia(trabajador.getAnosExperiencia());
        response.setTrabajadorZona(trabajador.getZonaTrabajo());

        return response;
    }
}