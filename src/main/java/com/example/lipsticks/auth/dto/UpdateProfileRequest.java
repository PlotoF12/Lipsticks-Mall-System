package com.example.lipsticks.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 16)
    private String gender;

    @Size(max = 32)
    private String skinTone;

    @Size(max = 32)
    private String skinType;
}
