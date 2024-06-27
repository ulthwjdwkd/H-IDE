package org.example.backend.user.repository;


import org.example.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByUserId(String userid);

    Boolean existsByUserNickname(String nickname);

    Boolean existsByUserNameAndUserIdAndUserEmail(String username, String userId, String email);

    //loginId을 받아 DB 테이블에서 회원을 조회하는 메소드 작성
    User findByUserId(String loginId);

    Optional<User> findByUserNameAndUserEmail(String userId, String email);

    Optional<User> findByUsersId(int usersId);
}
