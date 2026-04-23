package com.example.lipsticks.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {

    private Long id;
    private String username;
    private String role;
    private Boolean enabled;
    private String gender;
    private String skinTone;
    private String skinType;
}
