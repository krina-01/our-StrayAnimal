package com.springboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdoptionApplication {

    private Integer applicationId;

    private Integer userId;

    private Integer surrenderId;

    private LocalDateTime applicationTime;

    private String auditStatus="pending";

    private LocalDateTime auditTime;

    private LocalDateTime agreementSignTime;

    private String agreementContent;

    private String agreementStatus;
    
    private String adoptionReason;
    
    private SurrenderInfo surrender;
    
    private User user;
}
