package org.cinos.core.auth.service;

import org.cinos.core.auth.controller.request.LoginRequest;
import org.cinos.core.auth.controller.response.LoginResponse;
import org.cinos.core.auth.controller.response.RegisterResponse;
import org.cinos.core.users.controller.request.UserCreateRequest;
import org.cinos.core.users.dto.UserDTO;
import org.cinos.core.users.dto.mapper.UserMapper;
import org.cinos.core.users.repository.UserRepository;
import org.cinos.core.users.service.impl.UserService;
import org.cinos.core.users.utils.exceptions.DuplicateUserException;
import org.cinos.core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.model.Role;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public RegisterResponse register(final UserCreateRequest userCreateRequest) throws PasswordDontMatchException, DuplicateUserException {
        final UserDTO userDTO = userService.createUser(userCreateRequest);
        final String accessToken = jwtService.generateToken(userDTO);
        final String refreshToken = jwtService.generateRefreshToken(userDTO);
        return RegisterResponse.builder()
                .id(userDTO.id())
                .username(userDTO.username())
                .email(userDTO.email())
                .name(userDTO.name())
                .roles(userDTO.roles().stream().map(Enum::name).toList())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public LoginResponse login(final LoginRequest loginRequest) throws UserNotFoundException {
        final Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
        authenticationManager.authenticate(authentication);

        final UserDTO user = userService.getByUsername(loginRequest.username());
        final String accessToken = jwtService.generateToken(user);
        final String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .name(user.name())
                .lastname(user.lastname())
                .username(user.username())
                .email(user.email())
                .roles(user.roles().stream().map(Enum::name).toList())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public LoginResponse loginWithGoogle(String email, String name, String pictureUrl) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        UserEntity user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = UserEntity.builder()
                    .email(email)
                    .username(email) // O genera uno único
                    .name(name)
                    .roles(List.of(Role.USER))
                    .password("") // No password para Google
                    .active(true)
                    .build();
            user = userRepository.save(user);
            // Si tienes lógica para crear Account, llámala aquí
        }
        var userDTO = userMapper.toDTO(user);
        String accessToken = jwtService.generateToken(userDTO);
        String refreshToken = jwtService.generateRefreshToken(userDTO);
        return LoginResponse.builder()
                .name(user.getName())
                .lastname(user.getLastname())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Enum::name).toList())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String refreshToken(String refreshToken) throws UserNotFoundException {
        if (!jwtService.isValidRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }
        String username = jwtService.extractUsername(refreshToken);
        UserDTO user = userService.getByUsername(username);

        return jwtService.generateToken(user);
    }
}
