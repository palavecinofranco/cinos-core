package org.cinos.core.posts.service;

import org.cinos.core.posts.dto.CommentDTO;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICommentService {
    Page<CommentDTO> getCommentsByPostId(Long postId, Pageable page);
    Integer getCommentsLength(Long postId);
    CommentDTO createComment(CommentDTO commentDTO) throws UserNotFoundException;

}
