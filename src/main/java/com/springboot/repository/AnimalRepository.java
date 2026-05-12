package com.springboot.repository;

import com.springboot.entity.Animal;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AnimalRepository {

    @Select("SELECT * FROM animal")
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
            @Result(property = "location", column = "location")
    })
    List<Animal> findAll();

    @Select("SELECT * FROM animal WHERE animal_id = #{id}")
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
            @Result(property = "location", column = "location")
    })
    Optional<Animal> findById(@Param("id") Integer id);

    @Select("SELECT * FROM animal WHERE adopt_status = #{adoptStatus}")
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
            @Result(property = "location", column = "location")
    })
    List<Animal> findByAdoptStatus(@Param("adoptStatus") String adoptStatus);

    @Insert("INSERT INTO animal (name, species, age, gender, health_status, adopt_status, rescue_record, entry_time, location) " +
            "VALUES (#{name}, #{species}, #{age}, #{gender}, #{healthStatus}, #{adoptStatus}, #{rescueRecord}, #{entryTime}, #{location})")
    @Options(useGeneratedKeys = true, keyProperty = "animalId")
    int insert(Animal animal);

    @Update("<script>" +
            "UPDATE animal " +
            "<set>" +
            "<if test='name != null'>name = #{name},</if>" +
            "<if test='species != null'>species = #{species},</if>" +
            "<if test='age != null'>age = #{age},</if>" +
            "<if test='gender != null'>gender = #{gender},</if>" +
            "<if test='healthStatus != null'>health_status = #{healthStatus},</if>" +
            "<if test='adoptStatus != null'>adopt_status = #{adoptStatus},</if>" +
            "<if test='rescueRecord != null'>rescue_record = #{rescueRecord},</if>" +
            "<if test='location != null'>location = #{location},</if>" +
            "</set>" +
            "WHERE animal_id = #{animalId}" +
            "</script>")
    int update(Animal animal);

    @Delete("DELETE FROM animal WHERE animal_id = #{id}")
    int deleteById(@Param("id") Integer id);
}
