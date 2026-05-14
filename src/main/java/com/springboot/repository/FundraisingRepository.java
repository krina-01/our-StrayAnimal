package com.springboot.repository;

import com.springboot.entity.Fundraising;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Mapper
public interface FundraisingRepository {

    @Select("SELECT f.*, u.username FROM fundraising f LEFT JOIN user u ON f.creator_id = u.user_id")
    @Results({
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "purpose", column = "purpose"),
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
            @Result(property = "purpose", column = "purpose"),
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
            @Result(property = "purpose", column = "purpose"),
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

    @Select("SELECT f.*, u.username FROM fundraising f LEFT JOIN user u ON f.creator_id = u.user_id WHERE f.creator_id = #{creatorId}")
    @Results({
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "purpose", column = "purpose"),
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
    List<Fundraising> findByCreatorId(@Param("creatorId") Integer creatorId);

    @Insert("INSERT INTO fundraising (title, content, purpose, target_amount, current_amount, start_time, end_time, status, activity_img, creator_id, create_time) " +
            "VALUES (#{title}, #{content}, #{purpose}, #{targetAmount}, #{currentAmount}, #{startTime}, #{endTime}, #{status}, #{activityImg}, #{creatorId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "fundraisingId")
    int insert(Fundraising fundraising);

    @Update("<script>" +
            "UPDATE fundraising " +
            "<set>" +
            "<if test='title != null'>title = #{title},</if>" +
            "<if test='content != null'>content = #{content},</if>" +
            "<if test='purpose != null'>purpose = #{purpose},</if>" +
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

    @Select("SELECT SUM(amount) FROM donation_record WHERE fundraising_id = #{fundraisingId}")
    BigDecimal sumDonatedAmount(@Param("fundraisingId") Integer fundraisingId);

    @Update("UPDATE fundraising SET status = #{status}, update_time = NOW() WHERE fundraising_id = #{fundraisingId}")
    int updateStatus(@Param("fundraisingId") Integer fundraisingId, @Param("status") String status);

    @Update("UPDATE fundraising SET current_amount = #{currentAmount}, update_time = NOW() WHERE fundraising_id = #{fundraisingId}")
    int updateCurrentAmount(@Param("fundraisingId") Integer fundraisingId, @Param("currentAmount") BigDecimal currentAmount);
}
