package com.springboot.repository;

import com.springboot.entity.AdoptionApplication;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AdoptionApplicationRepository {

    @Select("SELECT a.*, u.username, u.phone, u.email, s.surrender_reason, s.audit_status as surrender_audit_status " +
            "FROM adoption_application a " +
            "LEFT JOIN user u ON a.user_id = u.user_id " +
            "LEFT JOIN surrender_info s ON a.surrender_id = s.surrender_id")
    @Results({
            @Result(property = "applicationId", column = "application_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "surrenderId", column = "surrender_id"),
            @Result(property = "applicationTime", column = "application_time"),
            @Result(property = "auditStatus", column = "audit_status"),
            @Result(property = "auditTime", column = "audit_time"),
            @Result(property = "agreementSignTime", column = "agreement_sign_time"),
            @Result(property = "agreementContent", column = "agreement_content"),
            @Result(property = "agreementStatus", column = "agreement_status"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.phone", column = "phone"),
            @Result(property = "user.email", column = "email"),
            @Result(property = "surrenderInfo.surrenderReason", column = "surrender_reason"),
            @Result(property = "surrenderInfo.auditStatus", column = "surrender_audit_status")
    })
    List<AdoptionApplication> findAll();

    @Select("SELECT a.*, u.username, u.phone, u.email, s.surrender_reason, s.audit_status as surrender_audit_status " +
            "FROM adoption_application a " +
            "LEFT JOIN user u ON a.user_id = u.user_id " +
            "LEFT JOIN surrender_info s ON a.surrender_id = s.surrender_id " +
            "WHERE a.application_id = #{id}")
    @Results({
            @Result(property = "applicationId", column = "application_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "surrenderId", column = "surrender_id"),
            @Result(property = "applicationTime", column = "application_time"),
            @Result(property = "auditStatus", column = "audit_status"),
            @Result(property = "auditTime", column = "audit_time"),
            @Result(property = "agreementSignTime", column = "agreement_sign_time"),
            @Result(property = "agreementContent", column = "agreement_content"),
            @Result(property = "agreementStatus", column = "agreement_status"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.phone", column = "phone"),
            @Result(property = "user.email", column = "email"),
            @Result(property = "surrenderInfo.surrenderReason", column = "surrender_reason"),
            @Result(property = "surrenderInfo.auditStatus", column = "surrender_audit_status")
    })
    Optional<AdoptionApplication> findById(@Param("id") Integer id);

    @Select("SELECT a.*, u.username, u.phone, u.email, s.surrender_reason, s.audit_status as surrender_audit_status " +
            "FROM adoption_application a " +
            "LEFT JOIN user u ON a.user_id = u.user_id " +
            "LEFT JOIN surrender_info s ON a.surrender_id = s.surrender_id " +
            "WHERE a.audit_status = #{auditStatus}")
    @Results({
            @Result(property = "applicationId", column = "application_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "surrenderId", column = "surrender_id"),
            @Result(property = "applicationTime", column = "application_time"),
            @Result(property = "auditStatus", column = "audit_status"),
            @Result(property = "auditTime", column = "audit_time"),
            @Result(property = "agreementSignTime", column = "agreement_sign_time"),
            @Result(property = "agreementContent", column = "agreement_content"),
            @Result(property = "agreementStatus", column = "agreement_status"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.phone", column = "phone"),
            @Result(property = "user.email", column = "email"),
            @Result(property = "surrenderInfo.surrenderReason", column = "surrender_reason"),
            @Result(property = "surrenderInfo.auditStatus", column = "surrender_audit_status")
    })
    List<AdoptionApplication> findByAuditStatus(@Param("auditStatus") String auditStatus);

    @Select("SELECT a.*, u.username, u.phone, u.email, s.surrender_reason, s.audit_status as surrender_audit_status " +
            "FROM adoption_application a " +
            "LEFT JOIN user u ON a.user_id = u.user_id " +
            "LEFT JOIN surrender_info s ON a.surrender_id = s.surrender_id " +
            "WHERE a.user_id = #{userId}")
    @Results({
            @Result(property = "applicationId", column = "application_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "surrenderId", column = "surrender_id"),
            @Result(property = "applicationTime", column = "application_time"),
            @Result(property = "auditStatus", column = "audit_status"),
            @Result(property = "auditTime", column = "audit_time"),
            @Result(property = "agreementSignTime", column = "agreement_sign_time"),
            @Result(property = "agreementContent", column = "agreement_content"),
            @Result(property = "agreementStatus", column = "agreement_status"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.phone", column = "phone"),
            @Result(property = "user.email", column = "email"),
            @Result(property = "surrenderInfo.surrenderReason", column = "surrender_reason"),
            @Result(property = "surrenderInfo.auditStatus", column = "surrender_audit_status")
    })
    List<AdoptionApplication> findByUserId(@Param("userId") Integer userId);

    @Insert("INSERT INTO adoption_application (user_id, surrender_id, application_time, audit_status) " +
            "VALUES (#{userId}, #{surrenderId}, #{applicationTime}, #{auditStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "applicationId")
    int insert(AdoptionApplication adoptionApplication);

    @Update("<script>" +
            "UPDATE adoption_application " +
            "<set>" +
            "<if test='auditStatus != null'>audit_status = #{auditStatus},</if>" +
            "<if test='auditTime != null'>audit_time = #{auditTime},</if>" +
            "<if test='agreementSignTime != null'>agreement_sign_time = #{agreementSignTime},</if>" +
            "<if test='agreementContent != null'>agreement_content = #{agreementContent},</if>" +
            "<if test='agreementStatus != null'>agreement_status = #{agreementStatus},</if>" +
            "</set>" +
            "WHERE application_id = #{applicationId}" +
            "</script>")
    int update(AdoptionApplication adoptionApplication);

    @Delete("DELETE FROM adoption_application WHERE application_id = #{id}")
    int deleteById(@Param("id") Integer id);
}
