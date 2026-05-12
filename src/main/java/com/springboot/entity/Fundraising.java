package com.springboot.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Fundraising {
    private Integer fundraisingId;
    private String title;
    private String content;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String activityImg;
    private Integer creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private User creator;
}
