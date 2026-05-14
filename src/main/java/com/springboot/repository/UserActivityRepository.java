package com.springboot.repository;

import com.springboot.entity.UserActivity;
import lombok.Data;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface UserActivityRepository {

    // 报名活动（状态 pending）
    @Insert("INSERT INTO user_activity(user_id, activity_id, status) VALUES(#{userId}, #{activityId}, 'pending')")
    int register(@Param("userId") Integer userId, @Param("activityId") Integer activityId);

    // 取消报名（只能取消 pending 或 approved 状态，且活动未结束）
    @Delete("DELETE FROM user_activity WHERE user_id = #{userId} AND activity_id = #{activityId} AND status IN ('pending', 'approved')")
    int cancelRegistration(@Param("userId") Integer userId, @Param("activityId") Integer activityId);

    // 更新报名状态为 approved（管理员审核通过）
    @Update("UPDATE user_activity SET status = 'approved' WHERE user_id = #{userId} AND activity_id = #{activityId} AND status = 'pending'")
    int approveRegistration(@Param("userId") Integer userId, @Param("activityId") Integer activityId);

    // 签到（状态必须为 approved，且活动当天）
    @Update("UPDATE user_activity SET status = 'checked_in', checkin_time = NOW() WHERE user_id = #{userId} AND activity_id = #{activityId} AND status = 'approved'")
    int checkin(@Param("userId") Integer userId, @Param("activityId") Integer activityId);

    // 查询志愿者在某活动上的报名记录
    @Select("SELECT * FROM user_activity WHERE user_id = #{userId} AND activity_id = #{activityId}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "activityId", column = "activity_id"),
            @Result(property = "checkinTime", column = "checkin_time")
    })
    Optional<UserActivity> findByUserAndActivity(@Param("userId") Integer userId, @Param("activityId") Integer activityId);

    // 查询志愿者的所有报名记录（含活动详情，用于报名状态跟踪）
    @Select("SELECT ua.*, va.activity_name, va.start_time, va.end_time, va.location " +
            "FROM user_activity ua " +
            "JOIN volunteer_activity va ON ua.activity_id = va.activity_id " +
            "WHERE ua.user_id = #{userId} " +
            "ORDER BY va.start_time DESC")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "activityId", column = "activity_id"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "status", column = "status"),
            @Result(property = "checkinTime", column = "checkin_time"),
            @Result(property = "activityName", column = "activity_name"),
            @Result(property = "activityStartTime", column = "start_time"),
            @Result(property = "activityEndTime", column = "end_time"),
            @Result(property = "activityLocation", column = "location")
    })
    List<UserActivityWithActivity> findRegistrationsWithActivity(@Param("userId") Integer userId);

    // 统计累计服务时长（分钟）
    @Select("SELECT IFNULL(SUM(duration), 0) FROM user_activity WHERE user_id = #{userId} AND duration > 0")
    Integer getTotalDuration(@Param("userId") Integer userId);

    // 统计参与活动次数（有服务时长的记录）
    @Select("SELECT COUNT(*) FROM user_activity WHERE user_id = #{userId} AND duration > 0")
    Integer getParticipationCount(@Param("userId") Integer userId);

    // 查询已完成的服务记录（duration > 0 且有活动信息）
    @Select("SELECT ua.duration, va.activity_name, ua.checkin_time as start_time, va.end_time as end_time, va.location " +
            "FROM user_activity ua " +
            "JOIN volunteer_activity va ON ua.activity_id = va.activity_id " +
            "WHERE ua.user_id = #{userId} AND ua.duration > 0 " +
            "ORDER BY ua.checkin_time DESC")
    @Results({
            @Result(property = "duration", column = "duration"),
            @Result(property = "activityName", column = "activity_name"),
            @Result(property = "startTime", column = "start_time"),   // 映射到 checkin_time
            @Result(property = "endTime", column = "end_time"),       // 活动的结束时间
            @Result(property = "location", column = "location")
    })
    List<ServiceRecord> findServiceRecords(@Param("userId") Integer userId);

    // 计算活动时长：更新所有已签到且未计算时长的记录（根据活动起止时间）
    @Update("UPDATE user_activity ua " +
            "JOIN volunteer_activity va ON ua.activity_id = va.activity_id " +
            "SET ua.duration = TIMESTAMPDIFF(MINUTE, ua.checkin_time, va.end_time) " +
            "WHERE ua.activity_id = #{activityId} AND ua.status = 'checked_in' AND ua.duration = 0")
    int calculateDurationForActivity(@Param("activityId") Integer activityId);

    @Select("SELECT * FROM user_activity WHERE status = #{status}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "activityId", column = "activity_id"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "status", column = "status"),
            @Result(property = "checkinTime", column = "checkin_time")
    })
    List<UserActivity> findByStatus(@Param("status") String status);

    @Update("UPDATE user_activity ua " +
            "INNER JOIN volunteer_activity va ON ua.activity_id = va.activity_id " +
            "SET ua.duration = TIMESTAMPDIFF(MINUTE, va.start_time, va.end_time) " +
            "WHERE ua.status = 'checked_in' AND ua.duration IS NULL")
    int calculateDurationForCheckedIn();

    class UserActivityWithActivity extends UserActivity {
        private String activityName;
        private LocalDateTime activityStartTime;
        private LocalDateTime activityEndTime;
        private String activityLocation;
        // getters and setters...
    }

    @Data
    class ServiceRecord {
        private Integer duration;
        private String activityName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String location;
        // getters and setters...
    }

    @Select("SELECT ua.user_id, ua.activity_id, ua.status, " +
            "u.username, u.phone, " +
            "va.activity_name, va.start_time, va.end_time, va.location " +
            "FROM user_activity ua " +
            "JOIN user u ON ua.user_id = u.user_id " +
            "JOIN volunteer_activity va ON ua.activity_id = va.activity_id " +
            "WHERE ua.status = 'pending'")
    List<Map<String, Object>> findPendingRegistrations();
}