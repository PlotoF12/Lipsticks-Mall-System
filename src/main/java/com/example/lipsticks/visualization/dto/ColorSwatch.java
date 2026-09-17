package com.example.lipsticks.visualization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColorSwatch {

    private Long productId;
    private String title;
    private String brand;
    private String shade;
    private String colorHex;
    private String category;
}
