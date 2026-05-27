package org.cinos.core.follows.service;

import org.cinos.core.users.dto.DTOConverter;
import org.cinos.core.follows.dto.FollowDTO;
import org.cinos.core.follows.entity.FollowEntity;
import org.cinos.core.follows.repository.FollowRepository;
import org.cinos.core.users.dto.UserDTO;
import org.cinos.core.users.service.IUserService;
import org.cinos.core.users.service.impl.AccountService;
import org.cinos.core.users.utils.exceptions.UserFollowingException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService implements IFollowService {

    private final FollowRepository followRepository;
    private final IUserService userService;
    private final AccountService accountService;

    @Override
    public FollowDTO followUser(final Long fromUserId, final Long toUserId) throws UserFollowingException, UserNotFoundException {
        UserDTO fromUser = userService.getUserById(fromUserId); //corrobora si existen los usuarios
        UserDTO toUser = userService.getUserById(toUserId);
        if(followRepository.findByFromUserIdAndToUserId(fromUser.id(), toUser.id(), FollowDTO.class).isPresent()){
            throw new UserFollowingException("Usuario ya sigue a este usuario");
        }

        FollowEntity follow = followRepository.save(FollowEntity.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .createdAt(LocalDateTime.now())
                .build());

        accountService.incrementFollowings(fromUserId);
        accountService.incrementFollowers(toUserId);

        return DTOConverter.toDTO(follow, FollowDTO.class);
    }

    @Override
    public List<UserDTO> getFollowers(final Long id) throws UserNotFoundException {
        UserDTO user = userService.getUserById(id);
        List<Long> followersIds = followRepository.findByToUserId(user.id()).stream().map(FollowEntity::getFromUserId).toList();
        return followersIds.stream().map(followerId -> {
            try {
                return userService.getUserById(followerId);
            } catch (UserNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    @Override
    public List<UserDTO> getFollowings(final Long id) throws UserNotFoundException {
        UserDTO user = userService.getUserById(id);
        List<Long> followingsIds = followRepository.findByFromUserId(user.id()).stream().map(FollowEntity::getToUserId).toList();
        return followingsIds.stream().map(followingId-> {
            try {
                return userService.getUserById(followingId);
            } catch (UserNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    @Override
    public Boolean isFollowing(final Long fromUserId, final Long toUserId) {
        return followRepository.findByFromUserIdAndToUserId(fromUserId, toUserId, FollowDTO.class).isPresent();
    }

    @Override
    public FollowDTO unfollowUser(Long fromUserId, Long toUserId) {
        FollowEntity follow = followRepository.findByFromUserIdAndToUserId(fromUserId, toUserId)
                .orElseThrow(() -> new RuntimeException("No se encontro la relacion de seguimiento"));

        followRepository.delete(follow);

        accountService.decrementFollowings(fromUserId);
        accountService.decrementFollowers(toUserId);

        return DTOConverter.toDTO(follow, FollowDTO.class);
    }

}
