package org.example.backend.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.user.dto.req.*;
import org.example.backend.user.dto.res.ActivityInfoResDto;
import org.example.backend.user.dto.res.UserInfoResDto;
import org.example.backend.user.entity.User;
import org.example.backend.user.jwt.CustomUserDetails;
import org.example.backend.user.jwt.JWTUtil;
import org.example.backend.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
//    private final EmailService emailService;
//    private final Map<String, String> emailCodeMap = new HashMap<>();

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @PostMapping("/check-userid")
    public ResponseEntity<String> checkUserid(@RequestBody String userid){

        // 빈 문자열 체크
        if (userid == null || userid.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("아이디를 입력해주세요.");
        }

        boolean isExist = userService.isUseridExist(userid);
        if (!isExist) {
            return ResponseEntity.ok("사용 가능한 아이디입니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 사용 중인 아이디입니다.");
        }
    }

    @PostMapping("/check-nickname")
    public ResponseEntity<String> checkNickname(@RequestBody String nickname){

        // 빈 문자열 체크
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("닉네임을 입력해주세요.");
        }

        boolean isExist = userService.isNicknameExist(nickname);
        if (!isExist) {
            return ResponseEntity.ok("사용 가능한 닉네임입니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 사용 중인 닉네임입니다.");
        }
    }

    @PostMapping("/check-password")
    public ResponseEntity<String> checkPassword(@RequestBody String password){
        // 빈 문자열 체크
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호를 입력해주세요.");
        }

        String userId = userService.getLoginIdFromJwt(); // JWT에서 로그인 아이디 추출
        boolean isExist = userService.isPasswordExist(userId, password);
        if (isExist) {
            return ResponseEntity.status(HttpStatus.OK).body("비밀번호가 성공적으로 확인되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("입력하신 비밀번호가 올바르지 않습니다. 다시 시도해주세요.");
        }
    }

    @PostMapping("/email-authentication")
    public ResponseEntity<String> sendVerificationCode(@RequestBody String email) throws Exception {
        // 빈 문자열 체크 (올바른 이메일 형식인지는 체크 안함)
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일을 입력해주세요.");
        }

//        String authNum = emailService.sendEmailAuthentication(email);
//        emailCodeMap.put(email, authNum);
        return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");

    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody @Validated SingUpReqDto signUpReqDto){
        // 로그 추가
        System.out.println("Received sign-up request: " + signUpReqDto);

        if (userService.isUseridExist(signUpReqDto.getUserid())) {
            throw new RuntimeException("이미 존재하는 아이디 입니다. : " + signUpReqDto.getUserid());
        }
        if (userService.isNicknameExist(signUpReqDto.getUsername())) {
            throw new RuntimeException("이미 존재하는 닉네임 입니다. : " + signUpReqDto.getUsername());
        }

        // 인증코드 확인
//        if ((signUpReqDto.getCode()).equals(emailCodeMap.get(signUpReqDto.getEmail()))){
            userService.signUp(signUpReqDto);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원 가입에 실패했습니다.");
//        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginReqDto loginReqDto, HttpServletResponse response) {
        try {
            // 스프링 시큐리티에서 userid password를 검증하기 위해 token(객체)에 담아야 함
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginReqDto.loginId(), loginReqDto.password(), null);

            // token에 담은 검증을 위한 AuthenticationManager(검증담당)로 전달
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 인증 객체를 SecurityContext에 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal(); //usersId 가져오기
            int usersId = customUserDetails.getUsersId();  // usersId 가져오기


            // JWT 발급
            String token = jwtUtil.createJwt(loginReqDto.loginId(), authentication.getAuthorities().iterator().next().getAuthority(), usersId,86400000L);

            // 로그인 기록 갱신
            userService.login(loginReqDto);

            // 응답 헤더에 JWT 추가
            String id = loginReqDto.loginId();
            return ResponseEntity.ok().header("authorization", "Bearer " + token).header("UserId",id).header("UsersId", String.valueOf(usersId)).body("로그인에 성공하셨습니다.");


        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인에 실패했습니다.");
        }
    }

    @PostMapping("/find-userid")
    public ResponseEntity<String> findUserId(@RequestBody @Validated FindUserId findUserId){
        // 인증코드 확인
//        if ((findUserId.getCode()).equals(emailCodeMap.get(findUserId.getEmail()))) {
            Optional<User> isExist = userService.findUserId(findUserId.getUsername(), findUserId.getEmail());
            if (isExist.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body(isExist.get().getUserId());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 회원입니다.");
            }
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증코드가 올바르지 않습니다.");
//        }
    }

    @PostMapping("/find-password")
    public ResponseEntity<String> findPassword(@RequestBody @Validated FindPassword findPassword, HttpServletResponse response){

        Boolean isExist = userService.findPassword(findPassword.getUsername(), findPassword.getUserId(), findPassword.getEmail());
        if (isExist){
            // 임시 방편 (보안취약)
            response.addHeader("userId", findPassword.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body("새로운 비밀번호를 입력해주세요.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 회원입니다.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Validated ResetPassword resetPassword, HttpServletRequest request){
        String userId = request.getHeader("userId");
        userService.updatePassword(userId, resetPassword.getNewPassword());
        return ResponseEntity.status(HttpStatus.OK).body("비밀번호 재설정이 완료되었습니다.");
    }


    @GetMapping("/my-page/1")
    public ResponseEntity<UserInfoResDto> getUserInfo(){

        String userId = userService.getLoginIdFromJwt(); // JWT에서 로그인 아이디 추출
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @GetMapping("/my-page/2")
    public ResponseEntity<ActivityInfoResDto> getActivity(){

        String userId = userService.getLoginIdFromJwt(); // JWT에서 로그인 아이디 추출
        return ResponseEntity.ok(userService.getActivity(userId));
    }

    @PostMapping("my-page/nickname")
    public ResponseEntity<String> updateNickname(@RequestBody String nickname){
        // 빈 문자열 체크
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("닉네임을 입력해주세요.");
        }

        String userId = userService.getLoginIdFromJwt(); // JWT에서 로그인 아이디 추출
        userService.updateNickname(userId, nickname);
        return ResponseEntity.ok().body("닉네임 변경이 완료되었습니다.");
    }

    @PostMapping("my-page/email")
    public ResponseEntity<String> updateEmail(@RequestBody @Validated UpdateEmailDto updateEmailDto) throws Exception{

        // 인증코드 확인 후 이메일 변경
//        if ((updateEmailDto.getCode()).equals(emailCodeMap.get(updateEmailDto.getEmail()))){
            String userId = userService.getLoginIdFromJwt();
            userService.updateEmail(userId, updateEmailDto.getEmail());
            return ResponseEntity.ok("이메일 변경이 완료되었습니다.");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 변경에 실패했습니다.");
//        }

    }

    @PostMapping("my-page/password")
    public ResponseEntity<String> updatePassword(@RequestBody @Validated UpdatePasswordDto updatePasswordDto){
        String userId = userService.getLoginIdFromJwt();
        userService.updatePassword(userId, updatePasswordDto.getNewPassword());
        return ResponseEntity.ok().body("비밀번호 변경이 완료되었습니다.");
    }


    @PostMapping("/{userId}/increment-point")
    public ResponseEntity<String> incrementPoint() {
        String userId = userService.getLoginIdFromJwt();
        userService.incrementPoint(userId);
        return ResponseEntity.ok("인증이 완료되었습니다.");
    }

    // 알람 주기 업데이트 추가
    @PutMapping("/my-page/{userId}/alarm-cycle")
    public ResponseEntity<String> updateAlarmCycle(@PathVariable("userId") String userId, @RequestBody UpdateAlarmCycleDto request) {
        Integer alarmCycle = request.getAlarmCycle();
        if (alarmCycle == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("알람 주기를 입력해주세요.");
        }

        try {
            userService.updateAlarmCycle(userId, alarmCycle);
            return ResponseEntity.ok("알람 주기가 성공적으로 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 사용자가 설정한 알람 주기 전송
    @GetMapping("/my-page/{userId}/alarm-cycle")
    public ResponseEntity<Integer> getAlarmCycle(@PathVariable("userId") String userId) {
        Integer alarmCycle = userService.getAlarmCycle(userId);
        if (alarmCycle != null) {
            return ResponseEntity.ok(alarmCycle);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
