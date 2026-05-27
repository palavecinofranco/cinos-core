package org.cinos.core.messages.service.impl;

import lombok.RequiredArgsConstructor;
import org.cinos.core.messages.dto.ConversationDTO;
import org.cinos.core.messages.entity.ConversationEntity;
import org.cinos.core.messages.repository.ConversationRepository;
import org.cinos.core.messages.service.IConversationService;
import org.cinos.core.users.entity.AccountEntity;
import org.cinos.core.users.service.IAccountService;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final IAccountService accountService;

    @Transactional
    public ConversationDTO getOrCreateConversation(Long user1Id, Long user2Id) {
        // Buscar si ya existe la conversación
        Optional<ConversationEntity> optionalConversation = conversationRepository.findByParticipants(user1Id, user2Id);

        ConversationEntity conversation = optionalConversation.orElseGet(() -> {
            // Si no existe, crearla
            AccountEntity user1;
            AccountEntity user2;
            try {
                user1 = accountService.getAccountEntityById(user1Id);
                user2 = accountService.getAccountEntityById(user2Id);
            } catch (UserNotFoundException e) {
                throw new RuntimeException("Error al buscar usuarios para crear conversación", e);
            }

            ConversationEntity newConversation = ConversationEntity.builder()
                    .participants(Set.of(user1, user2))
                    .lastUpdated(ZonedDateTime.now(ZoneId.systemDefault()).toLocalDateTime())
                    .build();

            return conversationRepository.save(newConversation);
        });

        // Una vez que tenemos la conversación (ya sea nueva o existente), devolvemos un DTO
        AccountEntity receiver = conversation.getParticipants()
                .stream()
                .filter(p -> !p.getId().equals(user1Id)) // el otro usuario (no el logueado)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se pudo encontrar el receptor de la conversación"));

        return ConversationDTO.builder()
                .id(conversation.getId())
                .lastUpdated(conversation.getLastUpdated().atZone(ZoneId.systemDefault()))
                .receiverId(receiver.getId())
                .receiverName(receiver.getUser().getName() + " " + receiver.getUser().getLastname())
                .receiverAvatar(receiver.getAvatarImg())
                .build();
    }


    public List<ConversationDTO> getUserConversations(Long userId) {
        List<ConversationEntity> conversations = conversationRepository.findByParticipants_Id(userId);

        return conversations.stream()
                .map(conversation -> {
                    // Buscar al "otro" participante
                    AccountEntity receiver = conversation.getParticipants()
                            .stream()
                            .filter(p -> !p.getId().equals(userId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Receiver not found"));

                    // Buscar el último mensaje (opcional, depende de tu modelo)
                    String lastMessage = conversation.getMessages() != null && !conversation.getMessages().isEmpty()
                            ? conversation.getMessages().get(conversation.getMessages().size() - 1).getContent()
                            : "";

                    return ConversationDTO.builder()
                            .id(conversation.getId())
                            .lastUpdated(conversation.getLastUpdated().atZone(ZoneId.systemDefault()))
                            .lastMessage(lastMessage)
                            .receiverName(receiver.getUser().getName() + " " + receiver.getUser().getLastname()) // <- suponiendo que tenés un método getUser() en AccountEntity
                            .receiverAvatar(receiver.getAvatarImg()) // <- suponiendo que tenés avatarUrl en AccountEntity
                            .build();
                })
                .toList();
    }

    @Override
    public ConversationDTO getById(Long id, String username) {
        ConversationEntity conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Buscar al "otro" participante
        AccountEntity receiver = conversation.getParticipants()
                .stream()
                .filter(p -> !p.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        return ConversationDTO.builder()
                .id(conversation.getId())
                .lastUpdated(conversation.getLastUpdated().atZone(ZoneId.systemDefault()))
                .receiverId(receiver.getId())
                .receiverName(receiver.getUser().getName() + " " + receiver.getUser().getLastname()) // <- suponiendo que tenés un método getUser() en AccountEntity
                .receiverAvatar(receiver.getAvatarImg()) // <- suponiendo que tenés avatarUrl en AccountEntity
                .build();
    }

    @Override
    public void save(ConversationEntity conversationEntity) {
        conversationRepository.save(conversationEntity);
    }

    @Override
    public ConversationEntity getEntityById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }


}
