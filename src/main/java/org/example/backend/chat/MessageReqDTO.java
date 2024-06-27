package org.example.backend.chat;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class MessageReqDTO {
    private Long id;
    private String content;
    private Timestamp timestamp;
    private UserInfoResDTO user;
    // getters and setters
}