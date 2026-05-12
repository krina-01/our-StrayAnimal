package com.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SurrenderInfo {
    private Integer surrenderId;
    private Integer userId;
    private Integer animalId;
    private String surrenderReason;
    private LocalDateTime submitTime;
    private String auditStatus;
    private LocalDateTime auditTime;

    private User user;
    private Animal animal;
}
