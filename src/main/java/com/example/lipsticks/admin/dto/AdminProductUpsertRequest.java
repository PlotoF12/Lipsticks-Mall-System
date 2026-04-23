package com.example.lipsticks.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminProductUpsertRequest {

    @NotBlank
    @Size(max = 128)
    private String title;

    @Size(max = 64)
    private String brand;

    @Size(max = 64)
    private String shade;

    @Size(max = 16)
    private String colorHex;

    @Size(max = 32)
    private String category;

    @Size(max = 32)
    private String finishType;

    @Size(max = 2000)
    private String detail;

    @NotNull
    private Integer price;

    @NotNull
    private Integer stock;

    @NotNull
    private Boolean onSale;

    @Size(max = 64)
    private String suitableSkinTone;

    @Size(max = 32)
    private String suitableGender;

    @Size(max = 128)
    private String scene;

    @Size(max = 512)
    private String imageUrl;
}
