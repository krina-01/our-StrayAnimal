package com.springboot.repository;

import com.springboot.entity.DonationManage;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DonationManageRepository {

    @Select("SELECT * FROM donation_manage")
    @Results(id = "donationManageMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "auditTime", column = "audit_time")
    })
    List<DonationManage> findAll();

    @Select("SELECT * FROM donation_manage WHERE id = #{id}")
    @ResultMap("donationManageMap")
    Optional<DonationManage> findById(@Param("id") Integer id);

    @Select("SELECT * FROM donation_manage WHERE fundraising_id = #{fundraisingId}")
    @ResultMap("donationManageMap")
    List<DonationManage> findByFundraisingId(@Param("fundraisingId") Integer fundraisingId);

    @Insert("INSERT INTO donation_manage (fundraising_id, user_id, amount, status, title, content) " +
            "VALUES (#{fundraisingId}, #{userId}, #{amount}, #{status}, #{title}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DonationManage manage);

    @Update("UPDATE donation_manage SET status = #{status}, audit_time = #{auditTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("auditTime") java.time.LocalDateTime auditTime);
}