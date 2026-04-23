package com.example.lipsticks.tryon.controller;

import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.tryon.dto.LipPoint;
import com.example.lipsticks.tryon.dto.TryOnResult;
import com.example.lipsticks.tryon.service.TryOnService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tryon")
@RequiredArgsConstructor
public class TryOnController {

    private final TryOnService tryOnService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<TryOnResult> upload(
            @RequestParam Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("outerLip") String outerLipJson,
            @RequestParam("innerLip") String innerLipJson,
            Authentication authentication
    ) {
        try {
            List<LipPoint> outerLip = objectMapper.readValue(outerLipJson, new TypeReference<List<LipPoint>>() {});
            List<LipPoint> innerLip = objectMapper.readValue(innerLipJson, new TypeReference<List<LipPoint>>() {});
            String username = authentication != null ? authentication.getName() : "anonymous";
            return ApiResponse.ok(tryOnService.upload(productId, file, username, outerLip, innerLip));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("唇部坐标解析失败: " + e.getMessage(), e);
        }
    }
}
