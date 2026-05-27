package org.cinos.core.users.service.impl;

import org.apache.coyote.BadRequestException;
import org.cinos.core.mail.models.SendEmailRequest;
import org.cinos.core.mail.service.MailService;
import org.cinos.core.users.controller.request.UserCreateRequest;
import org.cinos.core.users.controller.request.RecommendationsPreferencesRequest;
import org.cinos.core.users.controller.request.PremiumNotificationPreferencesRequest;
import org.cinos.core.users.dto.DTOConverter;
import org.cinos.core.users.dto.UserDTO;
import org.cinos.core.users.dto.VerifyCodeRequest;
import org.cinos.core.users.dto.VerifyCodeResponse;
import org.cinos.core.users.dto.mapper.UserMapper;
import org.cinos.core.users.entity.PendingVerificationEntity;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.model.Role;
import org.cinos.core.users.repository.PendingVerificationRepository;
import org.cinos.core.users.repository.UserRepository;
import org.cinos.core.users.service.IAccountService;
import org.cinos.core.users.service.IUserService;
import org.cinos.core.users.utils.exceptions.DuplicateUserException;
import org.cinos.core.users.utils.exceptions.EmailExistException;
import org.cinos.core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final String USER_NOT_FOUND_MESSSAGE = "Usuario no encontrado";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IAccountService accountService;
    private final MailService mailSender;
    private final PendingVerificationRepository pendingVerificationRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDTO> getUsers() {
        List<UserEntity> usersDB = userRepository.findAll();
        return usersDB.stream().map(e -> DTOConverter.toDTO(e, UserDTO.class)).toList();
    }

    @Override
    public String getFullName(final Long id) throws UserNotFoundException {
        UserEntity user = userRepository.findById(id).orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND_MESSSAGE));
        return String.format("%s %s", user.getName(), user.getLastname());
    }

    @Override
    public List<UserDTO> getUsersPageable(Integer page, Integer size) {
        Page<UserEntity> usersDB = userRepository.findAll(PageRequest.of(page, size));
        return usersDB.stream().map(e -> DTOConverter.toDTO(e, UserDTO.class)).toList();
    }

    @Override
    public UserDTO getUserById(Long id) throws UserNotFoundException {
        return userMapper.toDTO(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSSAGE)));
    }

    @Override
    public UserDTO createUser(UserCreateRequest request) throws PasswordDontMatchException, DuplicateUserException {
        passwordsMatch(request.password(), request.repeatPassword());

        // Validación lógica extra para username: mínimo 4 caracteres y al menos 3 letras
        String username = request.username();
        if (username.length() < 4 || username.chars().filter(Character::isLetter).count() < 3) {
            throw new DuplicateUserException("El usuario debe tener al menos 4 caracteres y al menos 3 letras");
        }

        // Validación lógica extra para password: mínimo 6 caracteres y al menos 1 número
        String password = request.password();
        if (password.length() < 6 || password.chars().noneMatch(Character::isDigit)) {
            throw new PasswordDontMatchException("La contraseña debe tener al menos 6 caracteres y al menos un número");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("El nombre de usuario ya está en uso");
        }

        // Verificar si el email ya existe
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("El correo electrónico ya está en uso");
        }
        UserEntity userEntity = UserEntity.builder()
                .name(request.name())
                .lastname(request.lastname())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(List.of(Role.USER))
                .build();

        try {
            UserEntity userDB = userRepository.save(userEntity);
            accountService.createUserAccount(userDB);
            return DTOConverter.toDTO(userDB, UserDTO.class);
        } catch (DataIntegrityViolationException | UserNotFoundException e) {
            throw new DuplicateUserException("Usuario o email ya existente");
        }
    }

    @Override
    public UserDTO getByUsername(String username) throws UserNotFoundException {
        return userMapper.toDTO(userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSSAGE)));
    }

    @Override
    public UserEntity getByUsernameEntity(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND_MESSSAGE));
    }

    @Override
    public UserEntity getByIdEntity(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND_MESSSAGE));
    }

    @Override
    public UserDTO getLoggedUser(){
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        return userMapper.toDTO(userEntity);
    }

    @Override
    public UserDTO getPremiumNotificationPreferences() {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        return userMapper.toDTO(userEntity);
    }

    @Override
    public UserDTO updatePremiumNotificationPreferences(PremiumNotificationPreferencesRequest request) {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        userEntity.setPremiumNotificationBrand(request.getBrand());
        userEntity.setPremiumNotificationModel(request.getModel());
        userEntity.setPremiumNotificationCondition(request.getCondition());
        userRepository.save(userEntity);
        return userMapper.toDTO(userEntity);
    }

    @Override
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    @Override
    public LocalDateTime sendVerificationCode(String email) throws EmailExistException {
        if(userRepository.existsByEmail(email)){
            throw new EmailExistException("El correo electrónico ya está asociado a una cuenta existente");
        }
        String verificationCode = generateVerificationCode();
        Optional<PendingVerificationEntity> existingVerification = pendingVerificationRepository.findByEmail(email);
        PendingVerificationEntity pendingVerification;
        if (existingVerification.isPresent()){
            pendingVerification = existingVerification.get();
            pendingVerification.setCode(verificationCode);
            pendingVerification.setExpiry(LocalDateTime.now().plusMinutes(2));
        } else {
            pendingVerification = PendingVerificationEntity.builder()
                    .code(verificationCode)
                    .email(email)
                    .expiry(LocalDateTime.now().plusMinutes(2))
                    .build();
        }
        pendingVerificationRepository.save(pendingVerification);
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .to(new String[]{email})
                .subject("Código de verificación")
                .message("Tu código de verificación es: " + verificationCode)
                .build();
        try {
            mailSender.sendMail(sendEmailRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el código de verificación", e);
        }
        return pendingVerification.getExpiry();
    }

    @Override
    public VerifyCodeResponse verifyCode(VerifyCodeRequest verifyCodeRequest) throws UserNotFoundException {
        PendingVerificationEntity pendingVerification = pendingVerificationRepository.findByEmail(verifyCodeRequest.email()).orElseThrow(
                () -> new RuntimeException("No se encontró un código para este email")
        );

        if (LocalDateTime.now().isAfter(pendingVerification.getExpiry())) {
            return VerifyCodeResponse.builder()
                    .isValid(Boolean.FALSE)
                    .message("El código ha expirado")
                    .build();
        }

        if (!pendingVerification.getCode().equals(verifyCodeRequest.code())) {
            return VerifyCodeResponse.builder()
                    .isValid(Boolean.FALSE)
                    .message("Código incorrecto")
                    .build();
        }

        pendingVerificationRepository.save(pendingVerification);

        return VerifyCodeResponse.builder()
                .isValid(Boolean.TRUE)
                .build();
    }

    @Override
    public UserDTO updateRecommendationsPreferences(RecommendationsPreferencesRequest request) {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        userEntity.setPreferredBrand(request.preferredBrand());
        userEntity.setWantsUsedCars(request.wantsUsedCars());
        userEntity.setWantsNewCars(request.wantsNewCars());
        userEntity.setUseLocationForRecommendations(request.useLocationForRecommendations());
        userEntity.setHasSeenRecommendationsModal(true);
        userRepository.save(userEntity);
        return DTOConverter.toDTO(userEntity, UserDTO.class);
    }

    private void passwordsMatch(String password, String repeatPassword) throws PasswordDontMatchException {
        if(!StringUtils.hasText(password) || !StringUtils.hasText(repeatPassword)){
            throw new PasswordDontMatchException("Las contraseñas no pueden estar vacias");
        }
        if (!password.equals(repeatPassword)) {
            throw new PasswordDontMatchException("Las contraseñas no coinciden");
        }
    }

    @Scheduled(cron = "0 0 * * * *") // cada hora en punto
    @Transactional
    public void removeExpiredVerifications() {
        LocalDateTime now = LocalDateTime.now();
        List<PendingVerificationEntity> expired = pendingVerificationRepository.findAll().stream()
                .filter(v -> v.getExpiry().isBefore(now))
                .toList();

        pendingVerificationRepository.deleteAll(expired);
    }

    /**
     * Asigna el rol PREMIUM a un usuario si no lo tiene
     */
    @Transactional
    public void assignPremiumRole(Long userId) throws UserNotFoundException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSSAGE));
        if (!user.getRoles().contains(Role.PREMIUM)) {
            user.getRoles().add(Role.PREMIUM);
            userRepository.save(user);
        }
    }

}
