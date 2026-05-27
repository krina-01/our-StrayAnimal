package com.springboot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Animal {
    private Integer animalId;
    private String name;
    private String species;
    private Integer age;
    private String gender;
    private String healthStatus;
    private String adoptStatus;
    private String rescueRecord;
    private Date entryTime;
    private Integer userId;
    private String location;

    //
    public Animal(Integer animalId, String name, String species, Integer age, String gender, String healthStatus, String adoptStatus, String rescueRecord, Date entryTime, Integer userId, String location) {
        this.animalId = animalId;
        this.name = name;
        this.species = species;
        this.age = age;
        this.gender = gender;
        this.healthStatus = healthStatus;
        this.adoptStatus = adoptStatus;
        this.rescueRecord = rescueRecord;
        this.entryTime = entryTime;
        this.userId = userId;
        this.location = location;
    }
    public Animal() {
    }

    public Integer getAnimalId() {
        return animalId;
    }

    public void setAnimalId(Integer animalId) {
        this.animalId = animalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getAdoptStatus() {
        return adoptStatus;
    }

    public void setAdoptStatus(String adoptStatus) {
        this.adoptStatus = adoptStatus;
    }

    public String getRescueRecord() {
        return rescueRecord;
    }

    public void setRescueRecord(String rescueRecord) {
        this.rescueRecord = rescueRecord;
    }

    public Date getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}