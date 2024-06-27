package org.example.backend.user.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
public class FindUserId {
    @NotBlank(message = "이름을 입력해주세요.")
//    @Pattern(regexp = "^[a-zA-Z가-힣]*$", message = "이름은 영문자 또는 한글만 입력 가능합니다.")
    private String username;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private String code;
}
