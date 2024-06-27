package org.example.backend.chat;

import org.example.backend.user.entity.User;
import org.example.backend.user.jwt.CustomUserDetails;
import org.example.backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @Autowired
    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return userService.findUser(customUserDetails.getUsername());
        }
        return null;
    }

    private ResponseEntity<?> handleUnauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchLatestMessageAfterLogin(@RequestParam("content") String content) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Timestamp loginAt = user.getLoginAt();
        Optional<Message> message = messageService.searchLatestMessageAfterLogin(content, loginAt);

        return message.map(msg -> {
            UserInfoResDTO userInfoResDto = UserInfoResDTO.builder()
                    .userId(user.getUserId())
                    .userName(user.getUserName())
                    .nickname(user.getUserNickname())
                    .email(user.getUserEmail())
                    .build();

            MessageReqDTO messageReqDto = new MessageReqDTO();
            messageReqDto.setId(msg.getId());
            messageReqDto.setContent(msg.getContent());
            messageReqDto.setUser(userInfoResDto);
            messageReqDto.setTimestamp(msg.getTimestamp());

            return ResponseEntity.ok(messageReqDto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/more")
    public ResponseEntity<?> searchLatestMessageContaining(@RequestParam("content") String content,
                                                           @RequestParam("messageId") long messageId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Timestamp loginAt = user.getLoginAt();
        Optional<Message> message = messageService.searchLatestMessageAfterLoginAndBeforeTargetMessage(content, loginAt, messageId);

        return message.map(msg -> {
            UserInfoResDTO userInfoResDto = UserInfoResDTO.builder()
                    .userId(user.getUserId())
                    .userName(user.getUserName())
                    .nickname(user.getUserNickname())
                    .email(user.getUserEmail())
                    .build();

            MessageReqDTO messageReqDto = new MessageReqDTO();
            messageReqDto.setId(msg.getId());
            messageReqDto.setContent(msg.getContent());
            messageReqDto.setUser(userInfoResDto);
            messageReqDto.setTimestamp(msg.getTimestamp());

            return ResponseEntity.ok(messageReqDto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/before/{messageId}")
    public ResponseEntity<?> getMessagesBefore(@PathVariable("messageId") long messageId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Timestamp loginAt = user.getLoginAt();
        List<Message> messagesBefore = messageService.getMessagesBefore(messageId, loginAt);

        return messagesBefore.isEmpty() ? ResponseEntity.notFound().build() :
                ResponseEntity.ok(messagesBefore.stream()
                        .map(msg -> mapMessageToDto(msg, user))
                        .collect(Collectors.toList()));
    }

    @GetMapping("/after/{messageId}")
    public ResponseEntity<?> getMessagesAfter(@PathVariable("messageId") long messageId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Timestamp loginAt = user.getLoginAt();
        List<Message> messagesAfter = messageService.getMessagesAfter(messageId, loginAt);

        return messagesAfter.isEmpty() ? ResponseEntity.notFound().build() :
                ResponseEntity.ok(messagesAfter.stream()
                        .map(msg -> mapMessageToDto(msg, user))
                        .collect(Collectors.toList()));
    }

    private MessageReqDTO mapMessageToDto(Message message, User user) {
        UserInfoResDTO userInfoResDto = UserInfoResDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .nickname(user.getUserNickname())
                .email(user.getUserEmail())
                .build();

        MessageReqDTO messageReqDto = new MessageReqDTO();
        messageReqDto.setId(message.getId());
        messageReqDto.setContent(message.getContent());
        messageReqDto.setUser(userInfoResDto);
        messageReqDto.setTimestamp(message.getTimestamp());

        return messageReqDto;
    }
}