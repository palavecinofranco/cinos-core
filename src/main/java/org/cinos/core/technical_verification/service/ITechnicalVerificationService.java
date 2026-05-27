package org.cinos.core.technical_verification.service;

import org.cinos.core.posts.models.VerificationStatus;
import org.cinos.core.technical_verification.dto.OrderVerificationRequest;
import org.cinos.core.technical_verification.dto.TechnicalVerificationPercentsDTO;
import org.cinos.core.technical_verification.dto.TechnicalVerificationRequest;
import org.cinos.core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.core.technical_verification.dto.VerificationStatusResponse;

import java.time.LocalDateTime;

public interface ITechnicalVerificationService {
    void orderVerification(OrderVerificationRequest orderVerificationRequest) throws PostNotFoundException;
    void acceptVerification(Long postId, LocalDateTime verificationAppointment) throws PostNotFoundException;
    void processVerification(TechnicalVerificationRequest request) throws PostNotFoundException;
    TechnicalVerificationPercentsDTO getPercentsByPostId(Long postId);
    VerificationStatusResponse getStatusByPostId(Long postId) throws PostNotFoundException;
}
