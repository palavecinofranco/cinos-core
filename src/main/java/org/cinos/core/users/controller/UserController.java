package org.cinos.core.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.BadRequestException;
import org.cinos.core.users.controller.request.PremiumNotificationPreferencesRequest;
import org.cinos.core.users.controller.request.UserCreateRequest;
import org.cinos.core.users.controller.request.RecommendationsPreferencesRequest;
import org.cinos.core.users.dto.*;
import org.cinos.core.users.entity.AccountEntity;
import org.cinos.core.users.service.IAccountService;
import org.cinos.core.users.service.IUserService;
import org.cinos.core.users.utils.exceptions.DuplicateUserException;
import org.cinos.core.users.utils.exceptions.EmailExistException;
import org.cinos.core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.cinos.core.users.dto.ContactInfoDTO;
import org.cinos.core.technical_verification.repository.TechnicalVerificationRepository;
import org.cinos.core.users.controller.PremiumStatsResponse;
import org.cinos.core.stripe.service.StripeService;
import com.stripe.exception.StripeException;
import org.cinos.core.posts.repository.PostRepository;
import org.cinos.core.users.repository.UserRepository;
import org.cinos.core.posts.entity.PostEntity;
import org.cinos.core.users.entity.UserEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IAccountService accountService;
    private final TechnicalVerificationRepository technicalVerificationRepository;
    private final StripeService stripeService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/fullname/{id}")
    public ResponseEntity<String> getFullName(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getFullName(id));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody final UserCreateRequest userRequest) throws PasswordDontMatchException, DuplicateUserException {
        return ResponseEntity.ok(userService.createUser(userRequest));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/logged")
    public ResponseEntity<UserDTO> getUserLogged() {
        return ResponseEntity.ok(userService.getLoggedUser());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/account/logged")
    public ResponseEntity<AccountDTO> getUserLoggedAccount() {
        return ResponseEntity.ok(accountService.getUserLoggedAccount());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/account/{id}")
    public ResponseEntity<AccountDTO> getUserAccount(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(accountService.getUserAccount(id));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/account/update")
    public ResponseEntity<?> updateUserAccount(
            @RequestParam("account") String account,
            @RequestParam(value = "image", required = false) MultipartFile image) throws UserNotFoundException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        UpdateAccountDTO accountDTO = objectMapper.readValue(account, UpdateAccountDTO.class);
        accountService.updateUserAccount(accountDTO, image);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PatchMapping("/recommendations-preferences")
    public ResponseEntity<UserDTO> updateRecommendationsPreferences(@RequestBody RecommendationsPreferencesRequest request) {
        return ResponseEntity.ok(userService.updateRecommendationsPreferences(request));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PatchMapping("/account/contact-info")
    public ResponseEntity<?> updateContactInfo(@RequestBody ContactInfoDTO contactInfo) {
        accountService.updateContactInfo(contactInfo);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/account/contact-info")
    public ResponseEntity<ContactInfoDTO> getContactInfo() {
        return ResponseEntity.ok(accountService.getContactInfo());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/premium-notification-preferences")
    public ResponseEntity<UserDTO> getPremiumNotificationPreferences() {
        return ResponseEntity.ok(userService.getPremiumNotificationPreferences());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/premium-notification-preferences")
    public ResponseEntity<UserDTO> updatePremiumNotificationPreferences(@RequestBody PremiumNotificationPreferencesRequest request) {
        return ResponseEntity.ok(userService.updatePremiumNotificationPreferences(request));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/premium/stats")
    public ResponseEntity<PremiumStatsResponse> getPremiumStats() throws StripeException, UserNotFoundException {
        // Obtener usuario logueado
        var authentication = (org.springframework.security.authentication.UsernamePasswordAuthenticationToken) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (org.cinos.core.users.entity.UserEntity) authentication.getPrincipal();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime endOfPeriod;
        java.time.LocalDateTime nextResetDate;
        if (userEntity.getStripeSubscriptionId() != null) {
            Long renewalEpoch = stripeService.getSubscriptionNextRenewal(userEntity.getStripeSubscriptionId());
            nextResetDate = java.time.LocalDateTime.ofEpochSecond(renewalEpoch, 0, java.time.ZoneOffset.UTC);
        } else {
            // Fallback: ciclo mensual calendario
            endOfPeriod = now.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            nextResetDate = endOfPeriod;
        }

        return ResponseEntity.ok(PremiumStatsResponse.builder()
                .verificationsRemaining(userEntity.getTechnicalVerificationCredits())
                .verificationReportsRemaining(userEntity.getTechnicalVerificationReportsCredits())
                .nextResetDate(nextResetDate)
                .build());
    }

    @PostMapping("/send-verification-code/{email}")
    public ResponseEntity<?> sendVerificationCode(@PathVariable final String email) throws UserNotFoundException, EmailExistException {
        LocalDateTime expiry = userService.sendVerificationCode(email);
        return ResponseEntity.ok(Map.of("expiresAt", expiry.format(DateTimeFormatter.ISO_DATE_TIME)));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<VerifyCodeResponse> verifyCode(@RequestBody VerifyCodeRequest verifyCodeRequest) throws UserNotFoundException {
        return ResponseEntity.ok().body(userService.verifyCode(verifyCodeRequest));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/technical-verification-access/{postId}")
    public ResponseEntity<Boolean> hasTechnicalVerificationAccess(
            @AuthenticationPrincipal UserEntity principal,
            @PathVariable Long postId) {
        UserEntity user = userRepository.findById(principal.getId()).orElseThrow();
        PostEntity post = postRepository.findById(postId).orElseThrow();
        boolean hasAccess = user.getUnlockedTechnicalVerifications().contains(post);
        return ResponseEntity.ok(hasAccess);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/technical-verification-access/{postId}")
    public ResponseEntity<?> unlockTechnicalVerificationAccess(
            @AuthenticationPrincipal UserEntity principal,
            @PathVariable Long postId) {
        UserEntity user = userRepository.findById(principal.getId()).orElseThrow();
        PostEntity post = postRepository.findById(postId).orElseThrow();
        if (user.getTechnicalVerificationCredits() == null || user.getTechnicalVerificationCredits() <= 0) {
            return ResponseEntity.status(403).body("No tienes más informes técnicos disponibles para desbloquear este ciclo.");
        }
        if (!user.getUnlockedTechnicalVerifications().contains(post)) {
            user.getUnlockedTechnicalVerifications().add(post);
            user.setTechnicalVerificationReportsCredits(user.getTechnicalVerificationReportsCredits() - 1);
            userRepository.save(user);
        }
        return ResponseEntity.ok().build();
    }

}
