package org.example.backend.chat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter @Setter
public class UserInfoResDTO {
    private String userId;
    private String userName;
    private String nickname;
    private String email;
//    private String profileImage;
}
