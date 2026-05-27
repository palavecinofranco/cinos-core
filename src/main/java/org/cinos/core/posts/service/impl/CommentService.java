package org.cinos.core.posts.service.impl;

import lombok.RequiredArgsConstructor;
import org.cinos.core.posts.dto.CommentDTO;
import org.cinos.core.posts.entity.CommentEntity;
import org.cinos.core.posts.repository.CommentRepository;
import org.cinos.core.posts.service.ICommentService;
import org.cinos.core.users.service.impl.AccountService;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;
    private final AccountService accountService;
    
    // Cache para control de rate limiting por usuario
    private final ConcurrentHashMap<Long, LocalDateTime> userLastCommentTime = new ConcurrentHashMap<>();
    private static final int MIN_COMMENT_INTERVAL_SECONDS = 3; // 3 segundos entre comentarios
    private static final int MAX_COMMENT_LENGTH = 500; // Máximo 500 caracteres

    @Override
    public Page<CommentDTO> getCommentsByPostId(Long postId, Pageable page) {
        Page<CommentEntity> commentPage = commentRepository.findByPostId(postId, page);
        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
                .map(commentEntity -> {
                    try {
                        return CommentDTO.builder()
                                .id(commentEntity.getId())
                                .postId(commentEntity.getPostId())
                                .userId(commentEntity.getUserId())
                                .content(commentEntity.getContent())
                                .commentDate(commentEntity.getCommentDate().atZone(ZoneId.systemDefault()))
                                .accountAvatar(accountService.getUserAccount(commentEntity.getUserId()).avatarImg())
                                .userName(accountService.getUserAccount(commentEntity.getUserId()).name())
                                .build();
                    } catch (UserNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

            return new PageImpl<>(commentDTOs, page, commentPage.getTotalElements());
    }

    @Override
    public Integer getCommentsLength(Long postId) {
        return commentRepository.findByPostId(postId).size();
    }

    @Override
    public CommentDTO createComment(CommentDTO commentDTO) {
        // Validar contenido del comentario
        validateCommentContent(commentDTO.content());
        
        // Validar rate limiting
        validateRateLimit(commentDTO.userId());
        
        // Actualizar tiempo del último comentario
        userLastCommentTime.put(commentDTO.userId(), LocalDateTime.now());
        
        CommentEntity commentEntity = CommentEntity.builder()
                .postId(commentDTO.postId())
                .userId(commentDTO.userId())
                .content(commentDTO.content().trim())
                .commentDate(LocalDateTime.now())
                .build();

        commentRepository.save(commentEntity);
        return CommentDTO.builder()
                .id(commentEntity.getId())
                .postId(commentEntity.getPostId())
                .userId(commentEntity.getUserId())
                .content(commentEntity.getContent())
                .commentDate(commentEntity.getCommentDate().atZone(ZoneId.systemDefault()))
                .build();
    }
    
    /**
     * Valida el contenido del comentario
     */
    private void validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }
        
        if (content.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("El comentario no puede exceder " + MAX_COMMENT_LENGTH + " caracteres");
        }
        
        // Verificar que no sea solo espacios en blanco
        if (content.trim().length() == 0) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }
    }
    
    /**
     * Valida el rate limiting para evitar spam
     */
    private void validateRateLimit(Long userId) {
        LocalDateTime lastCommentTime = userLastCommentTime.get(userId);
        if (lastCommentTime != null) {
            LocalDateTime now = LocalDateTime.now();
            long secondsSinceLastComment = java.time.Duration.between(lastCommentTime, now).getSeconds();
            
            if (secondsSinceLastComment < MIN_COMMENT_INTERVAL_SECONDS) {
                throw new IllegalStateException("Debes esperar " + MIN_COMMENT_INTERVAL_SECONDS + " segundos entre comentarios");
            }
        }
    }
}
