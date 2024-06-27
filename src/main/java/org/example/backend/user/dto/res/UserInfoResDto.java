package org.example.backend.user.dto.res;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter @Setter
public class UserInfoResDto {
    private String userId;
    private String userName;
    private String nickname;
    private String email;
//    private String profileImage;
}
