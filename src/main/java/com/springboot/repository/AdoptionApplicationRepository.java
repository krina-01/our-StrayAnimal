package com.springboot.repository;

import com.springboot.entity.AdoptionApplication;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdoptionApplicationRepository {

    @Select("SELECT * FROM adoption_application")
    @Results(id = "adoptionApplicationResultMap", value = {
        @Result(property = "applicationId", column = "application_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "surrenderId", column = "surrender_id"),
        @Result(property = "applicationTime", column = "application_time"),
        @Result(property = "auditStatus", column = "audit_status"),
        @Result(property = "auditTime", column = "audit_time"),
        @Result(property = "auditRemark", column = "audit_remark"),
        @Result(property = "agreementSignTime", column = "agreement_sign_time"),
        @Result(property = "agreementContent", column = "agreement_content"),
        @Result(property = "agreementStatus", column = "agreement_status"),
        @Result(property = "surrender", column = "surrender_id", javaType = com.springboot.entity.SurrenderInfo.class,
                one = @One(select = "com.springboot.repository.AdoptionApplicationRepository.findSurrenderById")),
        @Result(property = "user", column = "user_id", javaType = com.springboot.entity.User.class,
                one = @One(select = "com.springboot.repository.AdoptionApplicationRepository.findUserById"))
    })
    List<AdoptionApplication> findAll();

    @Select("SELECT * FROM adoption_application WHERE application_id = #{applicationId}")
    @ResultMap("adoptionApplicationResultMap")
    AdoptionApplication findById(Integer applicationId);

    @Select("SELECT * FROM adoption_application WHERE audit_status = #{auditStatus}")
    @ResultMap("adoptionApplicationResultMap")
    List<AdoptionApplication> findByAuditStatus(String auditStatus);

    @Select("SELECT * FROM adoption_application WHERE user_id = #{userId}")
    @ResultMap("adoptionApplicationResultMap")
    List<AdoptionApplication> findByUserId(Integer userId);

    @Select("SELECT * FROM adoption_application WHERE surrender_id = #{surrenderId}")
    @ResultMap("adoptionApplicationResultMap")
    List<AdoptionApplication> findBySurrenderId(Integer surrenderId);

    @Select("SELECT a.* FROM adoption_application a " +
            "INNER JOIN surrender_info s ON a.surrender_id = s.surrender_id " +
            "INNER JOIN animal an ON s.animal_id = an.animal_id " +
            "WHERE a.user_id = #{userId} AND a.audit_status = 'approved' AND an.adopt_status = 'adopted'")
    @ResultMap("adoptionApplicationResultMap")
    List<AdoptionApplication> findAdoptedApplicationsByUserId(Integer userId);

    @Insert("INSERT INTO adoption_application(user_id, surrender_id, application_time, audit_status, " +
            "agreement_sign_time, agreement_content, agreement_status) " +
            "VALUES(#{userId}, #{surrenderId}, #{applicationTime}, #{auditStatus}, " +
            "#{agreementSignTime}, #{agreementContent}, #{agreementStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "applicationId")
    void insert(AdoptionApplication adoptionApplication);

    @Update("<script>" +
            "UPDATE adoption_application " +
            "<set>" +
            "<if test='auditStatus != null'>audit_status=#{auditStatus},</if>" +
            "<if test='auditTime != null'>audit_time=#{auditTime},</if>" +
            "<if test='auditRemark != null'>audit_remark=#{auditRemark},</if>" +
            "<if test='agreementSignTime != null'>agreement_sign_time=#{agreementSignTime},</if>" +
            "<if test='agreementContent != null'>agreement_content=#{agreementContent},</if>" +
            "<if test='agreementStatus != null'>agreement_status=#{agreementStatus},</if>" +
            "</set>" +
            "WHERE application_id=#{applicationId}" +
            "</script>")
    void update(AdoptionApplication adoptionApplication);

    @Delete("DELETE FROM adoption_application WHERE application_id = #{applicationId}")
    void deleteById(Integer applicationId);

    @Select("SELECT * FROM surrender_info WHERE surrender_id = #{surrenderId}")
    @Results(id = "surrenderInfoResultMap", value = {
        @Result(property = "surrenderId", column = "surrender_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "animalId", column = "animal_id"),
        @Result(property = "surrenderReason", column = "surrender_reason"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "auditStatus", column = "audit_status"),
        @Result(property = "auditTime", column = "audit_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "animal", column = "animal_id", javaType = com.springboot.entity.Animal.class,
                one = @One(select = "com.springboot.repository.AdoptionApplicationRepository.findAnimalById")),
        @Result(property = "user", column = "user_id", javaType = com.springboot.entity.User.class,
                one = @One(select = "com.springboot.repository.AdoptionApplicationRepository.findUserById"))
    })
    com.springboot.entity.SurrenderInfo findSurrenderById(Integer surrenderId);

    @Select("SELECT animal_id AS animalId, name, species, age, gender, health_status AS healthStatus, " +
            "adopt_status AS adoptStatus, rescue_record AS rescueRecord, entry_time AS entryTime, " +
            "location, user_id AS userId FROM animal WHERE animal_id = #{animalId}")
    com.springboot.entity.Animal findAnimalById(Integer animalId);

    @Select("SELECT * FROM `user` WHERE user_id = #{userId}")
    com.springboot.entity.User findUserById(Integer userId);
}
