package com.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DonationManage {
    private Integer id;
    private Integer fundraisingId;
    private Integer userId;
    private BigDecimal amount;
    private Integer status;     // 0待审核 1通过 2拒绝 3已打款
    private String title;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime auditTime;
}