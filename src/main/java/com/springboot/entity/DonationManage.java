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
    private Integer status;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime auditTime;

    private User user;
    private Fundraising fundraising;
}
