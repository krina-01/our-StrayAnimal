package com.springboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VolunteerActivity {
    private Integer activityId;
    private String activityName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String description;
    private String recruitStatus;  // recruiting, closed
    private Integer creatorId;

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecruitStatus() {
        return recruitStatus;
    }

    public void setRecruitStatus(String recruitStatus) {
        this.recruitStatus = recruitStatus;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public VolunteerActivity(Integer activityId, String activityName, LocalDateTime startTime, LocalDateTime endTime, String location, String description, String recruitStatus, Integer creatorId) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.description = description;
        this.recruitStatus = recruitStatus;
        this.creatorId = creatorId;
    }

    public VolunteerActivity() {
    }
}