package com.example.lipsticks.tryon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TryOnResult {

    private Long productId;
    private String productTitle;
    private String colorHex;
    private String input;
    private String output;
    private String message;
}
