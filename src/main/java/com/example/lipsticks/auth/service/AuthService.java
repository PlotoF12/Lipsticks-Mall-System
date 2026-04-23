package com.example.lipsticks.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.auth.dto.ChangePasswordRequest;
import com.example.lipsticks.auth.dto.LoginRequest;
import com.example.lipsticks.auth.dto.LoginResponse;
import com.example.lipsticks.auth.dto.RegisterRequest;
import com.example.lipsticks.auth.dto.UpdateProfileRequest;
import com.example.lipsticks.auth.dto.UserMeResponse;
import com.example.lipsticks.security.JwtService;
import com.example.lipsticks.user.entity.UserAccount;
import com.example.lipsticks.user.entity.UserProfile;
import com.example.lipsticks.user.mapper.UserAccountMapper;
import com.example.lipsticks.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ROLE_USER = "USER";

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;

    @Transactional
    public void register(RegisterRequest request) {
        Long count = userAccountMapper.selectCount(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, request.getUsername())
        );
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        UserAccount account = new UserAccount();
        account.setUsername(request.getUsername());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setRole(ROLE_USER);
        account.setEnabled(true);
        userAccountMapper.insert(account);

        UserProfile profile = new UserProfile();
        profile.setUsername(request.getUsername());
        userProfileMapper.insert(profile);
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, request.getUsername())
        );
        if (account == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (Boolean.FALSE.equals(account.getEnabled())) {
            throw new IllegalArgumentException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = jwtService.generateToken(account.getUsername(), account.getRole());
        return new LoginResponse(token, "Bearer", account.getUsername(), account.getRole());
    }

    public UserMeResponse getMe(String username) {
        UserAccount account = requireAccount(username);
        UserProfile profile = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUsername, username)
        );
        return new UserMeResponse(
                account.getUsername(),
                account.getRole(),
                profile == null ? null : profile.getGender(),
                profile == null ? null : profile.getSkinTone(),
                profile == null ? null : profile.getSkinType()
        );
    }

    @Transactional
    public void updateProfile(String username, UpdateProfileRequest request) {
        requireAccount(username);
        UserProfile profile = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUsername, username)
        );
        if (profile == null) {
            profile = new UserProfile();
            profile.setUsername(username);
            profile.setGender(request.getGender());
            profile.setSkinTone(request.getSkinTone());
            profile.setSkinType(request.getSkinType());
            userProfileMapper.insert(profile);
            return;
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getSkinTone() != null) {
            profile.setSkinTone(request.getSkinTone());
        }
        if (request.getSkinType() != null) {
            profile.setSkinType(request.getSkinType());
        }
        userProfileMapper.updateById(profile);
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        UserAccount account = requireAccount(username);
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userAccountMapper.updateById(account);
    }

    private UserAccount requireAccount(String username) {
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, username)
        );
        if (account == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return account;
    }
}
