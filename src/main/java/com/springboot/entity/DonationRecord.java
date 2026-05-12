package com.springboot.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DonationRecord {
    private Integer id;
    private Integer fundraisingId;
    private Integer userId;
    private BigDecimal amount;
    private LocalDateTime payTime;
    private String message;
    private Boolean isAnonymous;
    private LocalDateTime createTime;

    private User user;
    private Fundraising fundraising;
}
