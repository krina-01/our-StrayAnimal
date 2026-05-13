package com.springboot.repository;

import com.springboot.entity.SurrenderInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

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
    SurrenderInfo findById(Integer surrenderId);

    @Select("SELECT * FROM surrender_info WHERE user_id = #{userId}")
    @ResultMap("surrenderInfoResultMap")
    List<SurrenderInfo> findByUserId(Integer userId);

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
            "</set>" +
            "WHERE surrender_id=#{surrenderId}" +
            "</script>")
    void update(SurrenderInfo surrenderInfo);

    @Delete("DELETE FROM surrender_info WHERE surrender_id = #{surrenderId}")
    void deleteById(Integer surrenderId);
}
