package org.example.backend.web;

import org.example.backend.chat.Message;
import org.example.backend.chat.MessageRepository;
import org.example.backend.chat.MessageReqDTO;
import org.example.backend.chat.UserInfoResDTO;
import org.example.backend.user.repository.UserRepository;
import org.example.backend.user.entity.User;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class WebSocketService {

    private final SimpMessageSendingOperations messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public WebSocketService(SimpMessageSendingOperations messagingTemplate, MessageRepository messageRepository, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public void sendLoginMessage(String loginId) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUserId(loginId));
        userOptional.ifPresent(user -> {
            userRepository.save(user);

            Map<String, String> messageContent = new HashMap<>();
            messageContent.put("content", "User " + loginId + " has entered the chat");
            messagingTemplate.convertAndSend("/sub/chat", messageContent);
        });
    }

    public void sendMessage(MessageReqDTO messageReqDto, Principal principal) {
        try {
            String userId = principal.getName();
            Optional<User> userOptional = Optional.ofNullable(userRepository.findByUserId(userId));
            userOptional.ifPresent(user -> {
                Message message = new Message();
                message.setContent(messageReqDto.getContent());
                message.setUser(user);
                Message savedMessage = messageRepository.save(message);

                UserInfoResDTO userInfoResDto = UserInfoResDTO.builder().
                        userId(userId).userName(user.getUserName()).email(user.getUserEmail())
                        .nickname(user.getUserNickname()).build();

                messageReqDto.setId(savedMessage.getId());
                messageReqDto.setTimestamp(savedMessage.getTimestamp());
                messageReqDto.setUser(userInfoResDto);
                messagingTemplate.convertAndSend("/sub/chat", messageReqDto);
            });

            if (!userOptional.isPresent()) {
                sendErrorMessage(principal, "User not found");
            }
        } catch (Exception e) {
            sendErrorMessage(principal, "Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendErrorMessage(Principal principal, String errorMessage) {
        String userId = principal.getName();
        Map<String, String> errorContent = new HashMap<>();
        errorContent.put("error", errorMessage);
        messagingTemplate.convertAndSendToUser(userId, "/sub/chat", errorContent);
    }
}