package com.springboot.entity;

import lombok.Data;

@Data
public class User {

    private Integer userId;

    private String username;

    private String password;

    private String phone;

    private String email;

    private String role = "user";

    private Boolean isVolunteer = false;

    private String gender = "unknown";

    private Boolean hasFixedIncome = false;

    private Integer birthYear;

    private Boolean isPetExperience = false;

    private String address;

    private String registerStatus = "pending";
}
