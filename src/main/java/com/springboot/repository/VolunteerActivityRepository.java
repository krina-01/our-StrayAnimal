package com.springboot.repository;

import com.springboot.entity.VolunteerActivity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface VolunteerActivityRepository {

    // ==================== 查询方法 ====================

    @Select("SELECT *, " +
            "CASE " +
            "  WHEN NOW() > end_time THEN 'closed' " +
            "  ELSE recruit_status " +
            "END as effective_recruit_status " +
            "FROM volunteer_activity ORDER BY start_time DESC")
    @Results({
            @Result(property = "activityId", column = "activity_id"),
            @Result(property = "activityName", column = "activity_name"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "recruitStatus", column = "effective_recruit_status"), // 使用计算后的状态
            @Result(property = "creatorId", column = "creator_id")
    })
    List<VolunteerActivity> findAll();

    @Select("SELECT * FROM volunteer_activity WHERE activity_id = #{id}")
    @Results({
            @Result(property = "activityId", column = "activity_id"),
            @Result(property = "activityName", column = "activity_name"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "recruitStatus", column = "recruit_status"),
            @Result(property = "creatorId", column = "creator_id")
    })
    Optional<VolunteerActivity> findById(@Param("id") Integer id);

    @Select("SELECT * FROM volunteer_activity WHERE recruit_status = #{status} ORDER BY start_time DESC")
    @Results({
            @Result(property = "activityId", column = "activity_id"),
            @Result(property = "activityName", column = "activity_name"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "recruitStatus", column = "recruit_status"),
            @Result(property = "creatorId", column = "creator_id")
    })
    List<VolunteerActivity> findByRecruitStatus(@Param("status") String status);

    @Select("SELECT *, " +
            "CASE " +
            "  WHEN NOW() < start_time THEN 'upcoming' " +
            "  WHEN NOW() BETWEEN start_time AND end_time THEN 'ongoing' " +
            "  ELSE 'ended' " +
            "END as dynamic_status " +
            "FROM volunteer_activity ORDER BY start_time DESC")
    @Results({
            @Result(property = "activityId", column = "activity_id"),
            @Result(property = "activityName", column = "activity_name"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "recruitStatus", column = "recruit_status"),
            @Result(property = "creatorId", column = "creator_id")
    })
    List<VolunteerActivity> findAllWithDynamicStatus();

    // ==================== 新增方法（管理员操作） ====================

    /**
     * 插入新活动（自动生成 activityId）
     */
    @Insert("INSERT INTO volunteer_activity(activity_name, start_time, end_time, location, description, recruit_status, creator_id) " +
            "VALUES(#{activityName}, #{startTime}, #{endTime}, #{location}, #{description}, #{recruitStatus}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "activityId")
    int insert(VolunteerActivity activity);

    /**
     * 更新活动信息（不包括 creator_id）
     */
    @Update("UPDATE volunteer_activity SET " +
            "activity_name = #{activityName}, " +
            "start_time = #{startTime}, " +
            "end_time = #{endTime}, " +
            "location = #{location}, " +
            "description = #{description}, " +
            "recruit_status = #{recruitStatus} " +
            "WHERE activity_id = #{activityId}")
    int update(VolunteerActivity activity);

    /**
     * 删除活动（若有关联的 user_activity 记录，需要数据库外键级联删除或先手动删除）
     */
    @Delete("DELETE FROM volunteer_activity WHERE activity_id = #{id}")
    int deleteById(@Param("id") Integer id);

    /**
     * 仅更新招募状态
     */
    @Update("UPDATE volunteer_activity SET recruit_status = #{status} WHERE activity_id = #{id}")
    int updateRecruitStatus(@Param("id") Integer id, @Param("status") String status);
}