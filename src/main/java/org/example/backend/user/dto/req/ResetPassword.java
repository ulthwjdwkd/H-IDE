package org.example.backend.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ResetPassword {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 6, message = "비밀번호는 6글자 이상이여야 합니다.")
//    @Pattern(
//            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*,.?]).+$",
//            message = "비밀번호는 영문, 숫자, 특정 특수문자(~!@#$%^&*,.?)를 각각 최소 하나 이상 포함해야 합니다."
//    )
    private String newPassword;

    @NotBlank(message = "확인 비밀번호를 입력해주세요.")
    private String checkNewPassword;
}
