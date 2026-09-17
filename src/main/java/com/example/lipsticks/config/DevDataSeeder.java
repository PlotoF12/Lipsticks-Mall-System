package com.example.lipsticks.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.user.entity.UserAccount;
import com.example.lipsticks.user.entity.UserProfile;
import com.example.lipsticks.user.mapper.UserAccountMapper;
import com.example.lipsticks.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAccount("admin", "Admin@123", "ADMIN", null, null, null);
        seedAccount("user", "User@123", "USER", "female", "neutral", "normal");
        seedAccount("xiaoming", "User@123", "USER", "male", "warm", "oily");
        seedAccount("xiaohong", "User@123", "USER", "female", "cool", "dry");
        seedAccount("test", "Test@123", "USER", "male", "neutral", "normal");
    }

    private void seedAccount(String username, String rawPassword, String role,
                             String gender, String skinTone, String skinType) {
        Long exists = userAccountMapper.selectCount(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, username)
        );
        if (exists != null && exists > 0) {
            return;
        }
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setRole(role);
        account.setEnabled(true);
        userAccountMapper.insert(account);

        UserProfile profile = new UserProfile();
        profile.setUsername(username);
        profile.setGender(gender);
        profile.setSkinTone(skinTone);
        profile.setSkinType(skinType);
        userProfileMapper.insert(profile);
    }
}
