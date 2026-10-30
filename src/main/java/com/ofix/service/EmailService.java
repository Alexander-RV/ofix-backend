package com.ofix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    // URL del front. En producción se define con ofix.frontend-url
    @Value("${ofix.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoRecuperacion(
            String destinatario,
            String token) {

        SimpleMailMessage mensaje =
                new SimpleMailMessage();

        mensaje.setTo(destinatario);
        mensaje.setSubject(
                "Recuperación de contraseña - OFIX"
        );

        String enlace =
            frontendUrl + "/reset-password?token="
            + token;

        mensaje.setText(
                "Hola,\n\n"
                + "Recibimos una solicitud para recuperar "
                + "la contraseña de tu cuenta OFIX.\n\n"
                + "Haz clic en el siguiente enlace para "
                + "restablecer tu contraseña:\n\n"
                + enlace
                + "\n\n"
                + "Este enlace tiene una validez de 15 minutos "
                + "y solo puede utilizarse una vez.\n\n"
                + "Si no solicitaste este cambio, puedes ignorar "
                + "este correo.\n\n"
                + "Equipo OFIX"
        );

        mailSender.send(mensaje);
    }

    // Aviso al profesional cuando el admin aprueba su registro
    @Async
    public void enviarRegistroAprobado(String destinatario, String nombre) {
        enviar(
                destinatario,
                "Tu registro en OFIX fue aprobado",
                "Hola " + nombre + ",\n\n"
                + "¡Buenas noticias! Revisamos tu solicitud y tu cuenta "
                + "de profesional en OFIX fue aprobada.\n\n"
                + "Ya puedes iniciar sesión y empezar a recibir solicitudes "
                + "de servicio:\n\n"
                + frontendUrl
                + "\n\n"
                + "Equipo OFIX"
        );
    }

    // Aviso al profesional cuando el admin rechaza su registro, con el motivo
    @Async
    public void enviarRegistroRechazado(
            String destinatario,
            String nombre,
            String motivo) {

        enviar(
                destinatario,
                "Actualización sobre tu registro en OFIX",
                "Hola " + nombre + ",\n\n"
                + "Gracias por registrarte como profesional en OFIX. "
                + "Revisamos tu solicitud y, por ahora, no pudimos aprobarla.\n\n"
                + "Motivo:\n"
                + motivo
                + "\n\n"
                + "Si crees que se trata de un error o quieres aportar más "
                + "información, responde a este correo.\n\n"
                + "Equipo OFIX"
        );
    }

    // Un fallo de correo nunca debe afectar la aprobación o el rechazo.
    private void enviar(String destinatario, String asunto, String texto) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(texto);
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo '{}': {}", asunto, e.getMessage());
        }
    }
}