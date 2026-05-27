package org.cinos.core.technical_verification.controller;

import lombok.RequiredArgsConstructor;
import org.cinos.core.posts.dto.AcceptVerificationRequest;
import org.cinos.core.posts.models.VerificationStatus;
import org.cinos.core.technical_verification.dto.OrderVerificationRequest;
import org.cinos.core.technical_verification.dto.TechnicalVerificationPercentsDTO;
import org.cinos.core.technical_verification.dto.TechnicalVerificationRequest;
import org.cinos.core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.core.technical_verification.dto.VerificationStatusResponse;
import org.cinos.core.technical_verification.service.ITechnicalVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/technical-verification")
@RequiredArgsConstructor
public class TechnicalVerificationController {

    private final ITechnicalVerificationService technicalVerificationService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/order")
    public ResponseEntity<Object> orderVerification(@RequestBody final OrderVerificationRequest orderVerificationRequest) throws PostNotFoundException {
        technicalVerificationService.orderVerification(orderVerificationRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/accept")
    public ResponseEntity<Object> acceptVerification(@RequestBody final AcceptVerificationRequest acceptVerificationRequest) throws PostNotFoundException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime parsedDate = LocalDateTime.parse(acceptVerificationRequest.appointmentDate(), formatter);
        technicalVerificationService.acceptVerification(acceptVerificationRequest.postId(), parsedDate);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/process")
    public ResponseEntity<Object> processVerification(@RequestBody final TechnicalVerificationRequest technicalVerificationRequest) throws PostNotFoundException {
        technicalVerificationService.processVerification(technicalVerificationRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/percents/{postId}")
    public ResponseEntity<TechnicalVerificationPercentsDTO> getPercentsByPostId(@PathVariable final Long postId) throws PostNotFoundException {
        return ResponseEntity.ok(technicalVerificationService.getPercentsByPostId(postId));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/status/{postId}")
    public ResponseEntity<VerificationStatusResponse> getStatusByPostId(@PathVariable final Long postId) throws PostNotFoundException {
        return ResponseEntity.ok(technicalVerificationService.getStatusByPostId(postId));
    }

}