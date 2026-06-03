package org.cinos.core.mail.models;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Builder
public record SendEmailRequest (
    String from,
    String[] to,
    String[] cc,
    String[] bcc,
    String subject,
    String message,
    List<MultipartFile> attachments,
    Map<String, byte[]> binaryAttachments,
    String templateName,
    Map<String, Object> templateVariables
) { }
