package com.springboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdoptionVisit {

    private Integer id;

    private Integer animalId;

    private Integer userId;

    private LocalDateTime visitTime;

    private String visitContent;

    private String animalStatus;

    private Animal animal;
}
