package com.springboot.repository;

import com.springboot.entity.Animal;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnimalRepository {
        //插入动物基本信息
        //插入动物基本信息
        //插入动物基本信息
        @Insert("INSERT INTO animal(name, species, age, gender, health_status, location, rescue_record, entry_time, user_id) " +
                "VALUES(#{name}, #{species}, #{age}, #{gender}, #{healthStatus}, #{location}, #{rescueRecord}, NOW(), #{userId})")
        @Options(useGeneratedKeys = true, keyProperty = "animalId")
        int insertAnimal(Animal animal);

        //
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
        //更新动物领养状态
        @Update("UPDATE animal SET adopt_status=#{adopt_status} WHERE animal_id=#{animal_id}")
        int updateAdoptStatus(@Param("adopt_status") String adopt_status, @Param("animal_id") Integer animal_id);
        //添加动物救助记录
        @Update("UPDATE animal SET rescue_record=#{rescue_record} WHERE animal_id=#{animal_id}")
        int addAnimalRescueRecord(@Param("rescue_record") String adopt_status, @Param("animal_id") Integer animal_id);
        //更新动物健康状态
        @Update("UPDATE animal SET health_status= #{health_status} WHERE animal_id= #{animal_id}")
        int updateHealthStatus(@Param("health_status") String health_status, @Param("animal_id") Integer animal_id);




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
