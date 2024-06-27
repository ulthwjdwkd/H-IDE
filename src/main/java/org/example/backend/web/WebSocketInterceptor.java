package org.example.backend.web;


import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.backend.user.dto.AuthDto;
import org.example.backend.user.jwt.CustomUserDetails;
import org.example.backend.user.jwt.JWTUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor {

    private final JWTUtil jwtUtil;

    @SneakyThrows
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authToken = accessor.getFirstNativeHeader("Authorization");
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                throw new AuthException("Missing or invalid Authorization header");
            }

            // Extract the token after "Hide " prefix
            String token = authToken.substring(7); // "Hide ".length() == 5

            if (jwtUtil.isExpired(token)) {
                throw new AuthException("Token expired");
            }

            String loginId = jwtUtil.getLoginId(token);
            String role = jwtUtil.getRole(token);


            // AutoDto 생성하여 값 set (토큰 요소인 userId, Role 빼고는 임의의 값 넣어도 됨.
            // 매번 DB 조회해서 정확한 값 가지고 오면 효율성 떨어짐)
            AuthDto authDto = AuthDto.builder().loginId(loginId).role(role).password("ABCabc!@#123").build();
            // UserDetails에 회원 정보 객체 담기
            CustomUserDetails customUserDetails = new CustomUserDetails(authDto);

            // 스프링 시큐리티 인증 토큰 생성 (현재 접근하는 주체의 정보와 권한을 담음)
            Authentication auth = new UsernamePasswordAuthenticationToken(customUserDetails, null,
                    customUserDetails.getAuthorities());

            // 세션에 사용자 등록
            accessor.setUser(auth);
        }

        return message;
    }
}