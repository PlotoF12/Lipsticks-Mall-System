package com.example.lipsticks.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendItem {

    private Long productId;
    private String title;
    private String brand;
    private String shade;
    private String colorHex;
    private String category;
    private Integer price;
    private String reason;
}
