package com.example.lipsticks.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMeResponse {

    private String username;
    private String role;
    private String gender;
    private String skinTone;
    private String skinType;
}
