package com.example.lipsticks.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.admin.dto.AdminUserResponse;
import com.example.lipsticks.admin.dto.AdminUserUpdateRequest;
import com.example.lipsticks.user.entity.UserAccount;
import com.example.lipsticks.user.entity.UserProfile;
import com.example.lipsticks.user.mapper.UserAccountMapper;
import com.example.lipsticks.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;

    public List<AdminUserResponse> listUsers() {
        List<UserAccount> accounts = userAccountMapper.selectList(Wrappers.emptyWrapper());
        List<UserProfile> profiles = userProfileMapper.selectList(Wrappers.emptyWrapper());
        Map<String, UserProfile> byUser = new HashMap<>();
        for (UserProfile p : profiles) {
            byUser.put(p.getUsername(), p);
        }
        return accounts.stream().map(a -> {
            UserProfile p = byUser.get(a.getUsername());
            return new AdminUserResponse(
                    a.getId(),
                    a.getUsername(),
                    a.getRole(),
                    a.getEnabled(),
                    p == null ? null : p.getGender(),
                    p == null ? null : p.getSkinTone(),
                    p == null ? null : p.getSkinType()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateUser(Long id, AdminUserUpdateRequest request) {
        UserAccount account = userAccountMapper.selectById(id);
        if (account == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (request.getEnabled() != null) {
            account.setEnabled(request.getEnabled());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            String r = request.getRole().toUpperCase();
            if (!"USER".equals(r) && !"ADMIN".equals(r)) {
                throw new IllegalArgumentException("角色只能是 USER 或 ADMIN");
            }
            account.setRole(r);
        }
        userAccountMapper.updateById(account);

        if (request.getGender() != null || request.getSkinTone() != null || request.getSkinType() != null) {
            UserProfile profile = userProfileMapper.selectOne(
                    Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUsername, account.getUsername())
            );
            if (profile == null) {
                profile = new UserProfile();
                profile.setUsername(account.getUsername());
                profile.setGender(request.getGender());
                profile.setSkinTone(request.getSkinTone());
                profile.setSkinType(request.getSkinType());
                userProfileMapper.insert(profile);
            } else {
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
        }
    }
}
