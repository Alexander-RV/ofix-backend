package com.ofix.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

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
            "http://localhost:3000/reset-password?token="
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
}