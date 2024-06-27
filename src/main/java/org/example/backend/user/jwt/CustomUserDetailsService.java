package org.example.backend.user.jwt;

import lombok.RequiredArgsConstructor;
import org.example.backend.user.repository.UserRepository;
import org.example.backend.user.dto.AuthDto;
import org.example.backend.user.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // DB에서 조회
        User userData = userRepository.findByUserId(loginId);

        if (userData != null) {
            AuthDto authDto = AuthDto.builder()
                    .loginId(userData.getUserId())
                    .password(userData.getUserPassword())
                    .loginRecord(userData.getLoginAt())
                    .role(userData.getUserRole())
                    .usersId(userData.getUsersId())  // usersId 추가
                    .build();

            // UserDetails에 담아서 return하면 AutneticationManager가 검증 함
            return new CustomUserDetails(authDto);
        }

        return null;
    }
}
