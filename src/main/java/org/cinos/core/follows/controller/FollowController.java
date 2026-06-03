package org.cinos.core.follows.controller;

import org.cinos.core.follows.dto.FollowDTO;
import org.cinos.core.follows.service.FollowService;
import org.cinos.core.users.dto.UserDTO;
import org.cinos.core.users.utils.exceptions.UserFollowingException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @deprecated La funcionalidad de seguir usuarios está deshabilitada en el frontend.
 *             Estos endpoints se conservan por compatibilidad pero no están en uso activo.
 */
@Deprecated
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /** @deprecated */
    @Deprecated
    @PostMapping
    public ResponseEntity<FollowDTO> followUser(@RequestParam final Long fromUserId, @RequestParam final Long toUserId) throws UserFollowingException, UserNotFoundException {
        return ResponseEntity.ok(followService.followUser(fromUserId, toUserId));
    }

    /** @deprecated */
    @Deprecated
    @DeleteMapping("/unfollow")
    public ResponseEntity<FollowDTO> unfollowUser(@RequestParam final Long fromUserId, @RequestParam final Long toUserId) throws UserFollowingException, UserNotFoundException {
        return ResponseEntity.ok(followService.unfollowUser(fromUserId, toUserId));
    }

    /** @deprecated */
    @Deprecated
    @GetMapping("/is-following")
    public ResponseEntity<Boolean> isFollowing(@RequestParam final Long fromUserId, @RequestParam final Long toUserId) throws UserFollowingException, UserNotFoundException {
        return ResponseEntity.ok(followService.isFollowing(fromUserId, toUserId));
    }

    /** @deprecated */
    @Deprecated
    @GetMapping("/followers/{id}")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(followService.getFollowers(id));
    }

    /** @deprecated */
    @Deprecated
    @GetMapping("/followings/{id}")
    public ResponseEntity<List<UserDTO>> getFollowings(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(followService.getFollowings(id));
    }

}
