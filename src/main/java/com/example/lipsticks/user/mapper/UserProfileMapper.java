package com.example.lipsticks.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lipsticks.user.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
