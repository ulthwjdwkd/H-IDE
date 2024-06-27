package org.example.backend.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Builder
@Getter @Setter
public class AuthDto {
    private String loginId;
    private String password;
    private Timestamp loginRecord;
    private String role;

    // usersid 추가
    private int usersId;
}
