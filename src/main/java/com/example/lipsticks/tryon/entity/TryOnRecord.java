package com.example.lipsticks.tryon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tryon_record")
public class TryOnRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private Long productId;
    private String originalFilename;
    private String resultFilename;
    private LocalDateTime createdAt;
}
