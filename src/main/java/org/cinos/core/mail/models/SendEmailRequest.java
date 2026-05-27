package org.cinos.core.mail.models;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
public record SendEmailRequest (
    String from,
    String[] to,
    String[] cc,
    String[] bcc,
    String subject,
    String message,
    List<MultipartFile> attachments
) { }
