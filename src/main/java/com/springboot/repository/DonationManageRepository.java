package com.springboot.repository;

import com.springboot.entity.DonationManage;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DonationManageRepository {

    @Select("SELECT d.*, u.username, f.title as fundraising_title " +
            "FROM donation_manage d " +
            "LEFT JOIN user u ON d.user_id = u.user_id " +
            "LEFT JOIN fundraising f ON d.fundraising_id = f.fundraising_id")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "auditTime", column = "audit_time"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "fundraising.title", column = "fundraising_title")
    })
    List<DonationManage> findAll();

    @Select("SELECT d.*, u.username, f.title as fundraising_title " +
            "FROM donation_manage d " +
            "LEFT JOIN user u ON d.user_id = u.user_id " +
            "LEFT JOIN fundraising f ON d.fundraising_id = f.fundraising_id " +
            "WHERE d.id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "auditTime", column = "audit_time"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "fundraising.title", column = "fundraising_title")
    })
    Optional<DonationManage> findById(@Param("id") Integer id);

    @Select("SELECT d.*, u.username, f.title as fundraising_title " +
            "FROM donation_manage d " +
            "LEFT JOIN user u ON d.user_id = u.user_id " +
            "LEFT JOIN fundraising f ON d.fundraising_id = f.fundraising_id " +
            "WHERE d.status = #{status}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "auditTime", column = "audit_time"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "fundraising.title", column = "fundraising_title")
    })
    List<DonationManage> findByStatus(@Param("status") Integer status);

    @Insert("INSERT INTO donation_manage (fundraising_id, user_id, amount, status, create_time) " +
            "VALUES (#{fundraisingId}, #{userId}, #{amount}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DonationManage donationManage);

    @Update("<script>" +
            "UPDATE donation_manage " +
            "<set>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='title != null'>title = #{title},</if>" +
            "<if test='content != null'>content = #{content},</if>" +
            "<if test='auditTime != null'>audit_time = #{auditTime},</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(DonationManage donationManage);

    @Delete("DELETE FROM donation_manage WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}
