package com.springboot.repository;

import com.springboot.entity.Fundraising;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FundraisingRepository {

    @Select("SELECT f.*, u.username FROM fundraising f LEFT JOIN user u ON f.creator_id = u.user_id")
    @Results({
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "targetAmount", column = "target_amount"),
            @Result(property = "currentAmount", column = "current_amount"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "activityImg", column = "activity_img"),
            @Result(property = "creatorId", column = "creator_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "creator.username", column = "username")
    })
    List<Fundraising> findAll();

    @Select("SELECT f.*, u.username FROM fundraising f LEFT JOIN user u ON f.creator_id = u.user_id WHERE f.fundraising_id = #{id}")
    @Results({
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "targetAmount", column = "target_amount"),
            @Result(property = "currentAmount", column = "current_amount"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "activityImg", column = "activity_img"),
            @Result(property = "creatorId", column = "creator_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "creator.username", column = "username")
    })
    Optional<Fundraising> findById(@Param("id") Integer id);

    @Select("SELECT f.*, u.username FROM fundraising f LEFT JOIN user u ON f.creator_id = u.user_id WHERE f.status = #{status}")
    @Results({
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "targetAmount", column = "target_amount"),
            @Result(property = "currentAmount", column = "current_amount"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "activityImg", column = "activity_img"),
            @Result(property = "creatorId", column = "creator_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "creator.username", column = "username")
    })
    List<Fundraising> findByStatus(@Param("status") String status);

    @Insert("INSERT INTO fundraising (title, content, target_amount, current_amount, start_time, end_time, status, activity_img, creator_id, create_time) " +
            "VALUES (#{title}, #{content}, #{targetAmount}, #{currentAmount}, #{startTime}, #{endTime}, #{status}, #{activityImg}, #{creatorId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "fundraisingId")
    int insert(Fundraising fundraising);

    @Update("<script>" +
            "UPDATE fundraising " +
            "<set>" +
            "<if test='title != null'>title = #{title},</if>" +
            "<if test='content != null'>content = #{content},</if>" +
            "<if test='targetAmount != null'>target_amount = #{targetAmount},</if>" +
            "<if test='currentAmount != null'>current_amount = #{currentAmount},</if>" +
            "<if test='startTime != null'>start_time = #{startTime},</if>" +
            "<if test='endTime != null'>end_time = #{endTime},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='activityImg != null'>activity_img = #{activityImg},</if>" +
            "</set>" +
            "WHERE fundraising_id = #{fundraisingId}" +
            "</script>")
    int update(Fundraising fundraising);

    @Delete("DELETE FROM fundraising WHERE fundraising_id = #{id}")
    int deleteById(@Param("id") Integer id);
}
