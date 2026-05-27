package org.cinos.core.users.service;

import org.apache.coyote.BadRequestException;
import org.cinos.core.users.controller.request.RecommendationsPreferencesRequest;
import org.cinos.core.users.controller.request.UserCreateRequest;
import org.cinos.core.users.dto.UserDTO;
import org.cinos.core.users.dto.VerifyCodeRequest;
import org.cinos.core.users.dto.VerifyCodeResponse;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.utils.exceptions.DuplicateUserException;
import org.cinos.core.users.utils.exceptions.EmailExistException;
import org.cinos.core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import org.cinos.core.users.controller.request.PremiumNotificationPreferencesRequest;

public interface IUserService {
    List<UserDTO> getUsers();
    String getFullName(Long id) throws UserNotFoundException;
    List<UserDTO> getUsersPageable(Integer page, Integer size);
    UserDTO getUserById(Long id) throws UserNotFoundException;
    UserDTO createUser(UserCreateRequest user) throws PasswordDontMatchException, DuplicateUserException;
    UserDTO getByUsername(String username) throws UserNotFoundException;
    UserEntity getByUsernameEntity(String username) throws UserNotFoundException;
    UserEntity getByIdEntity(Long id) throws UserNotFoundException;
    UserDTO getLoggedUser();
    String generateVerificationCode();
    LocalDateTime sendVerificationCode(String email) throws UserNotFoundException, EmailExistException;
    VerifyCodeResponse verifyCode(VerifyCodeRequest verifyCodeRequest) throws UserNotFoundException;
    UserDTO updateRecommendationsPreferences(RecommendationsPreferencesRequest request);
    UserDTO getPremiumNotificationPreferences();
    UserDTO updatePremiumNotificationPreferences(PremiumNotificationPreferencesRequest request);
}
