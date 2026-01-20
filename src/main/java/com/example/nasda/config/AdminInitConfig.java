package com.example.nasda.config;

import com.example.nasda.domain.UserEntity;
import com.example.nasda.domain.UserRepository;
import com.example.nasda.domain.UserRole;
import com.example.nasda.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            // 이메일이 "admin"인 계정이 없을 때만 생성
            if (userRepository.findByEmail("admin").isEmpty()) {

                UserEntity admin = UserEntity.builder()
                        .loginId("admin")                       // 로그인 ID
                        .email("admin")                         // 이메일
                        .password(passwordEncoder.encode("1234")) // 암호화된 비밀번호
                        .nickname("운영자")                      // 닉네임
                        .role(UserRole.ADMIN)                   // 관리자 권한
                        .status(UserStatus.ACTIVE)              // 활성 상태
                        .build();

                userRepository.save(admin);
            }
        };
    }
}