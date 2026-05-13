package com.springboot.repository;

import com.springboot.entity.AdoptionVisit;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AdoptionVisitRepository {

    @Select("SELECT v.id, v.animal_id AS animalId, v.user_id AS userId, " +
            "v.visit_time AS visitTime, v.visit_content AS visitContent, " +
            "v.animal_status AS animalStatus " +
            "FROM adoption_visit v " +
            "WHERE v.user_id = #{userId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "animalId", column = "animalId"),
            @Result(property = "userId", column = "userId"),
            @Result(property = "visitTime", column = "visitTime"),
            @Result(property = "visitContent", column = "visitContent"),
            @Result(property = "animalStatus", column = "animalStatus"),
            @Result(property = "animal", column = "animalId", javaType = com.springboot.entity.Animal.class,
                    one = @One(select = "com.springboot.repository.AdoptionVisitRepository.findAnimalById"))
    })
    List<AdoptionVisit> findByUserId(Integer userId);

    @Select("SELECT v.id, v.animal_id AS animalId, v.user_id AS userId, " +
            "v.visit_time AS visitTime, v.visit_content AS visitContent, " +
            "v.animal_status AS animalStatus " +
            "FROM adoption_visit v " +
            "INNER JOIN animal a ON v.animal_id = a.animal_id " +
            "INNER JOIN surrender_info s ON a.animal_id = s.animal_id " +
            "WHERE s.user_id = #{surrenderUserId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "animalId", column = "animalId"),
            @Result(property = "userId", column = "userId"),
            @Result(property = "visitTime", column = "visitTime"),
            @Result(property = "visitContent", column = "visitContent"),
            @Result(property = "animalStatus", column = "animalStatus"),
            @Result(property = "animal", column = "animalId", javaType = com.springboot.entity.Animal.class,
                    one = @One(select = "com.springboot.repository.AdoptionVisitRepository.findAnimalById"))
    })
    List<AdoptionVisit> findBySurrenderUserId(@Param("surrenderUserId") Integer surrenderUserId);

    @Select("SELECT v.id, v.animal_id AS animalId, v.user_id AS userId, " +
            "v.visit_time AS visitTime, v.visit_content AS visitContent, " +
            "v.animal_status AS animalStatus " +
            "FROM adoption_visit v " +
            "WHERE v.animal_id = #{animalId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "animalId", column = "animalId"),
            @Result(property = "userId", column = "userId"),
            @Result(property = "visitTime", column = "visitTime"),
            @Result(property = "visitContent", column = "visitContent"),
            @Result(property = "animalStatus", column = "animalStatus"),
            @Result(property = "animal", column = "animalId", javaType = com.springboot.entity.Animal.class,
                    one = @One(select = "com.springboot.repository.AdoptionVisitRepository.findAnimalById"))
    })
    List<AdoptionVisit> findByAnimalId(Integer animalId);

    @Select("SELECT animal_id AS animalId, name, species, age, gender, health_status AS healthStatus, " +
            "adopt_status AS adoptStatus, rescue_record AS rescueRecord, entry_time AS entryTime, " +
            "location, user_id AS userId " +
            "FROM animal WHERE animal_id = #{animalId}")
    com.springboot.entity.Animal findAnimalById(Integer animalId);

    @Insert("INSERT INTO adoption_visit (animal_id, user_id, visit_time, visit_content, animal_status) " +
            "VALUES (#{animalId}, #{userId}, #{visitTime}, #{visitContent}, #{animalStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdoptionVisit visit);

    @Update("UPDATE adoption_visit SET visit_content = #{visitContent}, animal_status = #{animalStatus}, " +
            "visit_time = #{visitTime} WHERE id = #{id}")
    int update(AdoptionVisit visit);
}