package org.cinos.core.posts.service;

import org.cinos.core.posts.controller.request.PostCreateRequest;
import org.cinos.core.posts.dto.PostDTO;
import org.cinos.core.posts.dto.PostFeedDTO;
import org.cinos.core.posts.dto.PostFilterDTO;
import org.cinos.core.posts.dto.PostProfileDTO;
import org.cinos.core.posts.entity.PostEntity;
import org.cinos.core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IPostService {
    List<PostDTO> getPostPageable(Integer page, Integer size);
    Page<PostFeedDTO> getFeedPosts(Long userId, Pageable pageable, Double userLatitude, Double userLongitude) throws UserNotFoundException ;
    Page<PostDTO> getFollowingsPosts(Long userId, Pageable pageable) throws UserNotFoundException;
    PostDTO getById(Long id) throws PostNotFoundException;
    List<PostDTO> getByUserId(Long userId);
    PostDTO createPost(PostCreateRequest request, List<MultipartFile> files) throws IOException, UserNotFoundException;
    List<PostProfileDTO> getPostsProfile(Long userId) throws UserNotFoundException;
    PostEntity getPostEntityById(Long id) throws PostNotFoundException;
    List<PostProfileDTO> getSavedPostsProfile(Long userId) throws UserNotFoundException;
    void saveUserPost(Long userId, Long postId) throws PostNotFoundException, UserNotFoundException;
    Boolean userSavedPost(Long userId, Long postId) throws PostNotFoundException, UserNotFoundException;
    void userUnsavePost(Long userId, Long postId) throws PostNotFoundException;
    void deactivatePost(Long postId) throws PostNotFoundException;
    void uploadDocumentation(Long postId, List<MultipartFile> files) throws PostNotFoundException, IOException;
    Page<PostDTO> getPostsFilter(PostFilterDTO postFilterDTO);
    List<PostDTO> searchPosts(String query);

}
