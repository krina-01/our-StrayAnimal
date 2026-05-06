package com.springboot.repository;

import com.springboot.entity.Animal;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AnimalRepository {

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
