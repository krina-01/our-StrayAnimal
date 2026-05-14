package com.springboot.repository;

import com.springboot.entity.Animal;
import com.springboot.entity.SurrenderInfo;
import com.springboot.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SurrenderInfoRepository {

    @Select("SELECT * FROM surrender_info")
    @Results(id = "surrenderInfoResultMap", value = {
        @Result(property = "surrenderId", column = "surrender_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "animalId", column = "animal_id"),
        @Result(property = "surrenderReason", column = "surrender_reason"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "auditStatus", column = "audit_status"),
        @Result(property = "auditTime", column = "audit_time"),
        @Result(property = "status", column = "status")
    })
    List<SurrenderInfo> findAll();

    @Select("SELECT * FROM surrender_info WHERE surrender_id = #{surrenderId}")
    @ResultMap("surrenderInfoResultMap")
    Optional<SurrenderInfo> findById(Integer surrenderId);

    @Select("SELECT * FROM surrender_info WHERE user_id = #{userId}")
    @ResultMap("surrenderInfoResultMap")
    List<SurrenderInfo> findByUserId(Integer userId);

    @Select("SELECT s.*, u.username, u.phone as user_phone, u.email as user_email, " +
            "a.name, a.species, a.gender, a.age, a.health_status, a.adopt_status, a.rescue_record, a.entry_time, a.location " +
            "FROM surrender_info s " +
            "LEFT JOIN user u ON s.user_id = u.user_id " +
            "LEFT JOIN animal a ON s.animal_id = a.animal_id " +
            "WHERE s.audit_status = #{auditStatus}")
    @Results(id = "surrenderInfoWithDetailsResultMap", value = {
        @Result(property = "surrenderId", column = "surrender_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "animalId", column = "animal_id"),
        @Result(property = "surrenderReason", column = "surrender_reason"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "auditStatus", column = "audit_status"),
        @Result(property = "auditTime", column = "audit_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "user", column = "user_id", javaType = User.class, one = @One(select = "selectUserById")),
        @Result(property = "animal", column = "animal_id", javaType = Animal.class, one = @One(select = "selectAnimalById"))
    })
    List<SurrenderInfo> findByAuditStatus(String auditStatus);

    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    @Results({
        @Result(property = "userId", column = "user_id"),
        @Result(property = "username", column = "username"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "email", column = "email")
    })
    User selectUserById(Integer userId);

    @Select("SELECT * FROM animal WHERE animal_id = #{animalId}")
    @Results({
        @Result(property = "animalId", column = "animal_id"),
        @Result(property = "name", column = "name"),
        @Result(property = "species", column = "species"),
        @Result(property = "age", column = "age"),
        @Result(property = "gender", column = "gender"),
        @Result(property = "healthStatus", column = "health_status"),
        @Result(property = "adoptStatus", column = "adopt_status"),
        @Result(property = "rescueRecord", column = "rescue_record"),
        @Result(property = "entryTime", column = "entry_time"),
        @Result(property = "location", column = "location"),
        @Result(property = "userId", column = "user_id")
    })
    Animal selectAnimalById(Integer animalId);

    @Select("SELECT * FROM surrender_info WHERE animal_id = #{animalId}")
    @ResultMap("surrenderInfoResultMap")
    List<SurrenderInfo> findByAnimalId(Integer animalId);

    @Insert("INSERT INTO surrender_info(user_id, animal_id, surrender_reason, submit_time, audit_status, status) " +
            "VALUES(#{userId}, #{animalId}, #{surrenderReason}, #{submitTime}, #{auditStatus}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "surrenderId")
    void insert(SurrenderInfo surrenderInfo);

    @Update("<script>" +
            "UPDATE surrender_info " +
            "<set>" +
            "<if test='auditStatus != null'>audit_status=#{auditStatus},</if>" +
            "<if test='auditTime != null'>audit_time=#{auditTime},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "<if test='surrenderReason != null'>surrender_reason = #{surrenderReason},</if>" +
            "<if test='auditStatus != null'>audit_status = #{auditStatus},</if>" +
            "<if test='auditTime != null'>audit_time = #{auditTime},</if>" +
            "</set>" +
            "WHERE surrender_id=#{surrenderId}" +
            "</script>")
    void update(SurrenderInfo surrenderInfo);

    @Delete("DELETE FROM surrender_info WHERE surrender_id = #{surrenderId}")
    void deleteById(Integer surrenderId);
}
