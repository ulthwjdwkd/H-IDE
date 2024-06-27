package org.example.backend.user.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.user.dto.res.ActivityInfoResDto;
import org.example.backend.user.dto.res.UserInfoResDto;
import org.example.backend.user.entity.Activity;
import org.example.backend.user.repository.UserActRepository;
import org.example.backend.user.repository.UserRepository;
import org.example.backend.user.dto.req.LoginReqDto;
import org.example.backend.user.dto.req.SingUpReqDto;
import org.example.backend.user.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserActRepository userActRepository;

    private final Map<String, String> emailCodeMap = new HashMap<>();


    public boolean isUseridExist(String userid) {
        return userRepository.existsByUserId(userid);
    }

    public boolean isNicknameExist(String nickname) {
        return userRepository.existsByUserNickname(nickname);
    }

    public boolean isPasswordExist(String userId, String password){
        User user = userRepository.findByUserId(userId);
        String userPassword = user.getUserPassword();
        return bCryptPasswordEncoder.matches(password, userPassword);
    }

    // 이건 뭐지?
    public User findUser(String loginId) {
        return userRepository.findByUserId(loginId);
    }

    @Transactional
    public boolean signUp(SingUpReqDto singUpReqDto) {
        User user = new User();
        user.setUserId(singUpReqDto.getUserid());
        user.setUserName(singUpReqDto.getUsername());
        user.setUserNickname(singUpReqDto.getNickname());
        user.setUserPassword(bCryptPasswordEncoder.encode(singUpReqDto.getPassword()));
        user.setUserEmail(singUpReqDto.getEmail());
        user.setUserRole("ROLE_ADMIN");

        Activity activity = new Activity(user);

        try {
            userRepository.save(user);
            userActRepository.save(activity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean login(LoginReqDto loginReqDto) {
        User user = userRepository.findByUserId(loginReqDto.loginId());
        user.setLoginAt(Timestamp.from(Instant.now()));
        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<User> findUserId(String userId, String email){
        return userRepository.findByUserNameAndUserEmail(userId, email);

    }

    public boolean findPassword(String username, String userId, String email){
        return userRepository.existsByUserNameAndUserIdAndUserEmail(username, userId, email);
    }

    public UserInfoResDto getUserInfo(String userId){
        User user = userRepository.findByUserId(userId);
        return UserInfoResDto.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .nickname(user.getUserNickname())
                .email(user.getUserEmail())
                .build();
    }

    public ActivityInfoResDto getActivity(String userId){
        User user = userRepository.findByUserId(userId);
        Activity activity = userActRepository.findByUser_UsersId(user.getUsersId());
        return ActivityInfoResDto.builder()
                .level(activity.getLevel())
                .point(activity.getPoint())
                .alarmCycle(user.getAlarmCycle())
                .build();
    }

    @Transactional
    public void updateNickname(String userId, String nickname){
        if (userRepository.existsByUserNickname(nickname)) {
            throw new RuntimeException("이미 존재하는 닉네임 입니다.");
        }
        User user = userRepository.findByUserId(userId);
        user.setUserNickname(nickname);
    }

    @Transactional
    public void updatePassword(String userId, String password){
        User user = userRepository.findByUserId(userId);
        user.setUserPassword(bCryptPasswordEncoder.encode(password));
    }

    @Transactional
    public void updateEmail(String userId, String email){
        User user = userRepository.findByUserId(userId);
        user.setUserEmail(email);
    }

    @Transactional
    public void removeUser(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user != null) {
            userRepository.delete(user); // 회원 정보 삭제
        } else {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + userId);
        }
    }

    public String getLoginIdFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw  new RuntimeException("Security Context 에 인증 정보가 없습니다.");
        }

        return authentication.getName();

    }


    @Transactional
    public void incrementPoint(String userId){
        User user = userRepository.findByUserId(userId);
        Activity activity = userActRepository.findByUser_UsersId(user.getUsersId());
        if(activity != null){
            activity.setPoint(activity.getPoint() + 1);
            userActRepository.save(activity);
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * ?") // 매 1시간마다 갱신
    public void updateLevel() {
        List<Activity> activities = userActRepository.findAll();
        for (Activity activity : activities) {
            int currentPoints = activity.getPoint();
            int newLevel = determineLevel(currentPoints);
            if (newLevel != activity.getLevel()) {
                activity.setLevel(newLevel);
                userActRepository.save(activity);
            }
        }
    }

    private int determineLevel(int points) {
        if (points >= 10) {
            return 5;
        } else if (points >= 8) {
            return 4;
        } else if (points >= 6) {
            return 3;
        } else if (points >= 4) {
            return 2;
        } else if (points >= 2) {
            return 1;
        } else {
            return 0;
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 초기화
    public void resetActivity(){
        LocalDateTime now = LocalDateTime.now();
        userActRepository.resetActivity(now);
    }

    //users_id로 사용자 찾기
    public Optional<User> getUserByUsersId(int usersId) {
        return userRepository.findByUsersId(usersId);
    }

    // 알람 주기 업데이트
    @Transactional
    public void updateAlarmCycle(String userId, int alarmCycle) {
        User user = userRepository.findByUserId(userId);
        if (user != null) {
            user.setAlarmCycle(alarmCycle);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + userId);
        }
    }

    //알람 주기 전송해주기
    public Integer getAlarmCycle(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user != null) {
            return user.getAlarmCycle();
        } else {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + userId);
        }
    }


}
