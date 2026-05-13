package com.springboot.repository;

import com.springboot.entity.Animal;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnimalRepository {

        @Insert("INSERT INTO animal(name,species,age,gender,health_status,adopt_status,rescue_record,entry_time) " +
                "VALUES(#{name},#{species},#{age},#{gender},#{healthStatus},#{adoptStatus},#{rescueRecord},#{entryTime})")
        int insertAnimal(Animal animal);

        // 根据ID删除
        @Delete("DELETE FROM animal WHERE animal_id = #{animalId}")
        int deleteById(Integer animalId);

        // 根据ID修改
        @Update("UPDATE animal SET name=#{name},species=#{species},age=#{age},gender=#{gender}," +
                "health_status=#{healthStatus},adopt_status=#{adoptStatus},rescue_record=#{rescueRecord},entry_time=#{entryTime} " +
                "WHERE animal_id=#{animalId}")
        int updateById(Animal animal);

        // 根据ID查询
        @Select("SELECT * FROM animal WHERE animal_id = #{animalId}")
        Animal selectById(Integer animalId);

        // 查询全部动物
        @Select("SELECT * FROM animal")
        List<Animal> selectAll();

        @Update("UPDATE animal SET adopt_status=#{adopt_status} WHERE animal_id=#{animal_id}")
        int updateAdoptStatus(@Param("adopt_status") String adopt_status, @Param("animal_id") Integer animal_id);


                @Select("SELECT animal_id AS animalId, name, species, age, gender, health_status AS healthStatus, " +
                        "adopt_status AS adoptStatus, rescue_record AS rescueRecord, entry_time AS entryTime, " +
                        "location, user_id AS userId FROM animal")
                List<Animal> findAll();

                @Select("SELECT animal_id AS animalId, name, species, age, gender, health_status AS healthStatus, " +
                        "adopt_status AS adoptStatus, rescue_record AS rescueRecord, entry_time AS entryTime, " +
                        "location, user_id AS userId FROM animal WHERE animal_id = #{animalId}")
                Animal findById(Integer animalId);

                @Select("SELECT a.animal_id AS animalId, a.name, a.species, a.age, a.gender, a.health_status AS healthStatus, " +
                        "a.adopt_status AS adoptStatus, a.rescue_record AS rescueRecord, a.entry_time AS entryTime, " +
                        "a.location, a.user_id AS userId " +
                        "FROM animal a " +
                        "INNER JOIN surrender_info s ON a.animal_id = s.animal_id " +
                        "WHERE a.adopt_status = 'available' AND s.status = 'published'")
                List<Animal> findAvailableAnimals();

                @Select("SELECT animal_id AS animalId, name, species, age, gender, health_status AS healthStatus, " +
                        "adopt_status AS adoptStatus, rescue_record AS rescueRecord, entry_time AS entryTime, " +
                        "location, user_id AS userId FROM animal WHERE user_id = #{userId}")
                List<Animal> findByUserId(Integer userId);

                @Update("UPDATE animal SET adopt_status = #{status} WHERE animal_id = #{animalId}")
                int updateAnimalStatus(@Param("animalId") Integer animalId, @Param("status") String status);
        }
