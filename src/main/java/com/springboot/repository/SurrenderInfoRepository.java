package com.springboot.repository;

import com.springboot.entity.SurrenderInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SurrenderInfoRepository {

    @Select("SELECT s.*, u.username, u.phone, u.email, a.name as animal_name, a.species " +
            "FROM surrender_info s " +
            "LEFT JOIN user u ON s.user_id = u.user_id " +
            "LEFT JOIN animal a ON s.animal_id = a.animal_id")
    @Results({
        @Result(property = "surrenderId", column = "surrender_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "animalId", column = "animal_id"),
        @Result(property = "surrenderReason", column = "surrender_reason"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "auditStatus", column = "audit_status"),
        @Result(property = "auditTime", column = "audit_time"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.phone", column = "phone"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "animal.name", column = "animal_name"),
        @Result(property = "animal.species", column = "species")
    })
    List<SurrenderInfo> findAll();

    @Select("SELECT s.*, u.username, u.phone, u.email, a.name as animal_name, a.species " +
            "FROM surrender_info s " +
            "LEFT JOIN user u ON s.user_id = u.user_id " +
            "LEFT JOIN animal a ON s.animal_id = a.animal_id " +
            "WHERE s.surrender_id = #{id}")
    @Results({
        @Result(property = "surrenderId", column = "surrender_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "animalId", column = "animal_id"),
        @Result(property = "surrenderReason", column = "surrender_reason"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "auditStatus", column = "audit_status"),
        @Result(property = "auditTime", column = "audit_time"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.phone", column = "phone"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "animal.name", column = "animal_name"),
        @Result(property = "animal.species", column = "species")
    })
    Optional<SurrenderInfo> findById(@Param("id") Integer id);

    @Select("SELECT s.*, u.username, u.phone, u.email, a.name as animal_name, a.species " +
            "FROM surrender_info s " +
            "LEFT JOIN user u ON s.user_id = u.user_id " +
            "LEFT JOIN animal a ON s.animal_id = a.animal_id " +
            "WHERE s.audit_status = #{auditStatus}")
    @Results({
        @Result(property = "surrenderId", column = "surrender_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "animalId", column = "animal_id"),
        @Result(property = "surrenderReason", column = "surrender_reason"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "auditStatus", column = "audit_status"),
        @Result(property = "auditTime", column = "audit_time"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.phone", column = "phone"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "animal.name", column = "animal_name"),
        @Result(property = "animal.species", column = "species")
    })
    List<SurrenderInfo> findByAuditStatus(@Param("auditStatus") String auditStatus);

    @Select("SELECT s.*, u.username, u.phone, u.email, a.name as animal_name, a.species " +
            "FROM surrender_info s " +
            "LEFT JOIN user u ON s.user_id = u.user_id " +
            "LEFT JOIN animal a ON s.animal_id = a.animal_id " +
            "WHERE s.user_id = #{userId}")
    @Results({
        @Result(property = "surrenderId", column = "surrender_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "animalId", column = "animal_id"),
        @Result(property = "surrenderReason", column = "surrender_reason"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "auditStatus", column = "audit_status"),
        @Result(property = "auditTime", column = "audit_time"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.phone", column = "phone"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "animal.name", column = "animal_name"),
        @Result(property = "animal.species", column = "species")
    })
    List<SurrenderInfo> findByUserId(@Param("userId") Integer userId);

    @Insert("INSERT INTO surrender_info (user_id, animal_id, surrender_reason, submit_time, audit_status) " +
            "VALUES (#{userId}, #{animalId}, #{surrenderReason}, #{submitTime}, #{auditStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "surrenderId")
    int insert(SurrenderInfo surrenderInfo);

    @Update("<script>" +
            "UPDATE surrender_info " +
            "<set>" +
            "<if test='surrenderReason != null'>surrender_reason = #{surrenderReason},</if>" +
            "<if test='auditStatus != null'>audit_status = #{auditStatus},</if>" +
            "<if test='auditTime != null'>audit_time = #{auditTime},</if>" +
            "</set>" +
            "WHERE surrender_id = #{surrenderId}" +
            "</script>")
    int update(SurrenderInfo surrenderInfo);

    @Delete("DELETE FROM surrender_info WHERE surrender_id = #{id}")
    int deleteById(@Param("id") Integer id);
}
