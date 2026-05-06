package com.springboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Animal {

    private Integer animalId;

    private String name;

    private String species;

    private Integer age;

    private String gender;

    private String healthStatus;

    private String adoptStatus="pending";

    private String rescueRecord;

    private LocalDateTime entryTime;

    private String location;
    private Integer userId;
}