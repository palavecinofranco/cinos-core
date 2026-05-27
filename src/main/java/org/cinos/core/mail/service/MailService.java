package org.cinos.core.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.cinos.core.mail.models.SendEmailRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendMail(SendEmailRequest sendEmailRequest) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            // El segundo parámetro true indica que tendremos multipart message (para adjuntos)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(sendEmailRequest.to());
            helper.setSubject(sendEmailRequest.subject());

            Context context = new Context();
            context.setVariable("message", sendEmailRequest.message());
            String content = templateEngine.process("email", context);
            helper.setText(content, true);

            // Adjuntar archivos
            if (sendEmailRequest.attachments() != null && !sendEmailRequest.attachments().isEmpty()) {
                for (MultipartFile file : sendEmailRequest.attachments()) {
                    if (!file.isEmpty()) {
                        InputStreamSource source = new ByteArrayResource(file.getBytes());
                        helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), source);
                    }
                }
            }

            javaMailSender.send(message);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }
}
