package org.cinos.core.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.cinos.core.mail.models.SendEmailRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    private static final String LOGO_CID = "cinos-logo";
    private static final String LOGO_PATH = "static/images/cinos-logo.png";

    @Async("emailExecutor")
    public void sendMail(SendEmailRequest sendEmailRequest) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(sendEmailRequest.to());
            helper.setSubject(sendEmailRequest.subject());

            Context context = new Context();
            String content;

            if (sendEmailRequest.templateName() != null && sendEmailRequest.templateVariables() != null) {
                sendEmailRequest.templateVariables().forEach(context::setVariable);
                content = templateEngine.process(sendEmailRequest.templateName(), context);
            } else {
                context.setVariable("message", sendEmailRequest.message());
                content = templateEngine.process("email", context);
            }

            helper.setText(content, true);

            // Logo inline CID para templates HTML
            ClassPathResource logoResource = new ClassPathResource(LOGO_PATH);
            if (logoResource.exists()) {
                helper.addInline(LOGO_CID, logoResource);
            }

            // Adjuntos MultipartFile
            if (sendEmailRequest.attachments() != null && !sendEmailRequest.attachments().isEmpty()) {
                for (MultipartFile file : sendEmailRequest.attachments()) {
                    if (!file.isEmpty()) {
                        InputStreamSource source = new ByteArrayResource(file.getBytes());
                        helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), source);
                    }
                }
            }

            // Adjuntos binarios (imágenes del vehículo, etc.)
            if (sendEmailRequest.binaryAttachments() != null && !sendEmailRequest.binaryAttachments().isEmpty()) {
                for (Map.Entry<String, byte[]> entry : sendEmailRequest.binaryAttachments().entrySet()) {
                    helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
                }
            }

            javaMailSender.send(message);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }
}
