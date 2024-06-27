package org.example.backend.web;

import org.example.backend.chat.MessageReqDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {

    private final WebSocketService webSocketService;

    public WebSocketController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @MessageMapping("/chat.login")
    public void sendLoginMessage(String loginId) {
        webSocketService.sendLoginMessage(loginId);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageReqDTO messageReqDto, Principal principal) {
        webSocketService.sendMessage(messageReqDto, principal);
    }
}