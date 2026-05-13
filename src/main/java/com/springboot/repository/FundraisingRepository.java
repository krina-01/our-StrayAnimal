package com.springboot.repository;

import com.springboot.entity.Fundraising;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Mapper
public interface FundraisingRepository {

    @Select("SELECT * FROM fundraising")
    @Results(id = "fundraisingMap", value = {
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
            @Result(property = "updateTime", column = "update_time")
    })
    List<Fundraising> findAll();

    @Select("SELECT * FROM fundraising WHERE fundraising_id = #{id}")
    @ResultMap("fundraisingMap")
    Optional<Fundraising> findById(@Param("id") Integer id);

    @Select("SELECT * FROM fundraising WHERE status = #{status}")
    @ResultMap("fundraisingMap")
    List<Fundraising> findByStatus(@Param("status") String status);

    @Insert("INSERT INTO fundraising (title, content, target_amount, current_amount, start_time, end_time, status, activity_img, creator_id) " +
            "VALUES (#{title}, #{content}, #{targetAmount}, #{currentAmount}, #{startTime}, #{endTime}, #{status}, #{activityImg}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "fundraisingId")
    int insert(Fundraising fundraising);

    @Update("UPDATE fundraising SET current_amount = #{currentAmount} WHERE fundraising_id = #{fundraisingId}")
    int updateCurrentAmount(@Param("fundraisingId") Integer fundraisingId, @Param("currentAmount") BigDecimal currentAmount);

    @Update("UPDATE fundraising SET status = #{status} WHERE fundraising_id = #{fundraisingId}")
    int updateStatus(@Param("fundraisingId") Integer fundraisingId, @Param("status") String status);

    @Select("SELECT SUM(amount) FROM donation_record WHERE fundraising_id = #{fundraisingId}")
    BigDecimal sumDonatedAmount(@Param("fundraisingId") Integer fundraisingId);
}