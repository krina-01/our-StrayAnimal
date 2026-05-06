package com.springboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserActivity {
    private Integer userId;
    private Integer activityId;
    private Integer duration;        // 服务时长（分钟）
    private String status;           // pending, approved, checked_in
    private LocalDateTime checkinTime;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(LocalDateTime checkinTime) {
        this.checkinTime = checkinTime;
    }

    public UserActivity(Integer userId, Integer activityId, Integer duration, String status, LocalDateTime checkinTime) {
        this.userId = userId;
        this.activityId = activityId;
        this.duration = duration;
        this.status = status;
        this.checkinTime = checkinTime;
    }

    public UserActivity() {
    }
}