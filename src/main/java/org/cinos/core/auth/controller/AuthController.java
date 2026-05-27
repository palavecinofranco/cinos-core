package org.cinos.core.auth.controller;

import org.cinos.core.auth.controller.request.LoginRequest;
import org.cinos.core.auth.controller.response.LoginResponse;
import org.cinos.core.auth.controller.response.RegisterResponse;
import org.cinos.core.auth.service.AuthService;
import org.cinos.core.users.controller.request.UserCreateRequest;
import org.cinos.core.users.utils.exceptions.DuplicateUserException;
import org.cinos.core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid final UserCreateRequest userCreateRequest) throws PasswordDontMatchException, DuplicateUserException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(userCreateRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid final LoginRequest userCreateRequest) throws UserNotFoundException {
        return ResponseEntity.ok(authService.login(userCreateRequest));
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody Map<String, String> body) {
        String idTokenString = body.get("idToken");
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList("681995243192-r7gk27btviuon6bvcffjooqhhh67prs2.apps.googleusercontent.com")) // <-- PON TU CLIENT_ID
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                LoginResponse loginResponse = authService.loginWithGoogle(email, name, pictureUrl);
                return ResponseEntity.ok(loginResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestParam String refreshToken) throws UserNotFoundException {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

}
