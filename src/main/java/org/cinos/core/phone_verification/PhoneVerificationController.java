package org.cinos.core.phone_verification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/phone-verification")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/send")
    public ResponseEntity<?> sendCode(@RequestParam String phone) {
        phoneVerificationService.sendCode(phone);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verifyCode(@RequestBody PhoneVerifyRequest request) {
        boolean verified = phoneVerificationService.verifyCode(request.phone(), request.code());
        return ResponseEntity.ok(Map.of("verified", verified));
    }
}
