package com.springboot.repository;

import com.springboot.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserRepository {

    @Select("SELECT * FROM user")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "email", column = "email"),
            @Result(property = "role", column = "role"),
            @Result(property = "isVolunteer", column = "is_volunteer"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "hasFixedIncome", column = "has_fixed_income"),
            @Result(property = "birthYear", column = "birth_year"),
            @Result(property = "isPetExperience", column = "is_pet_experience"),
            @Result(property = "address", column = "address"),
            @Result(property = "registerStatus", column = "register_status"),
            @Result(property = "volunteerApplyStatus", column = "volunteer_apply_status")
    })
    List<User> findAll();

    @Select("SELECT * FROM user WHERE user_id = #{id}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "email", column = "email"),
            @Result(property = "role", column = "role"),
            @Result(property = "isVolunteer", column = "is_volunteer"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "hasFixedIncome", column = "has_fixed_income"),
            @Result(property = "birthYear", column = "birth_year"),
            @Result(property = "isPetExperience", column = "is_pet_experience"),
            @Result(property = "address", column = "address"),
            @Result(property = "registerStatus", column = "register_status"),
            @Result(property = "volunteerApplyStatus", column = "volunteer_apply_status")
    })
    Optional<User> findById(@Param("id") Long id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "email", column = "email"),
            @Result(property = "role", column = "role"),
            @Result(property = "isVolunteer", column = "is_volunteer"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "hasFixedIncome", column = "has_fixed_income"),
            @Result(property = "birthYear", column = "birth_year"),
            @Result(property = "isPetExperience", column = "is_pet_experience"),
            @Result(property = "address", column = "address"),
            @Result(property = "registerStatus", column = "register_status"),
            @Result(property = "volunteerApplyStatus", column = "volunteer_apply_status")
    })
    Optional<User> findByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE role = #{role}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "email", column = "email"),
            @Result(property = "role", column = "role"),
            @Result(property = "isVolunteer", column = "is_volunteer"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "hasFixedIncome", column = "has_fixed_income"),
            @Result(property = "birthYear", column = "birth_year"),
            @Result(property = "isPetExperience", column = "is_pet_experience"),
            @Result(property = "address", column = "address"),
            @Result(property = "registerStatus", column = "register_status"),
            @Result(property = "volunteerApplyStatus", column = "volunteer_apply_status")
    })
    List<User> findByRole(@Param("role") String role);

    @Select("SELECT * FROM user WHERE is_volunteer = #{isVolunteer}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "email", column = "email"),
            @Result(property = "role", column = "role"),
            @Result(property = "isVolunteer", column = "is_volunteer"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "hasFixedIncome", column = "has_fixed_income"),
            @Result(property = "birthYear", column = "birth_year"),
            @Result(property = "isPetExperience", column = "is_pet_experience"),
            @Result(property = "address", column = "address"),
            @Result(property = "registerStatus", column = "register_status"),
            @Result(property = "volunteerApplyStatus", column = "volunteer_apply_status")
    })
    List<User> findByIsVolunteer(@Param("isVolunteer") Boolean isVolunteer);

    @Select("SELECT * FROM user WHERE register_status = #{registerStatus}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "email", column = "email"),
            @Result(property = "role", column = "role"),
            @Result(property = "isVolunteer", column = "is_volunteer"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "hasFixedIncome", column = "has_fixed_income"),
            @Result(property = "birthYear", column = "birth_year"),
            @Result(property = "isPetExperience", column = "is_pet_experience"),
            @Result(property = "address", column = "address"),
            @Result(property = "registerStatus", column = "register_status"),
            @Result(property = "volunteerApplyStatus", column = "volunteer_apply_status")
    })
    List<User> findByRegisterStatus(@Param("registerStatus") String registerStatus);


    @Insert("INSERT INTO user (username, password, phone, email, role, is_volunteer, gender, has_fixed_income, birth_year, is_pet_experience, address, register_status, volunteer_apply_status) " +
            "VALUES (#{username}, #{password}, #{phone}, #{email}, #{role}, #{isVolunteer}, #{gender}, #{hasFixedIncome}, #{birthYear}, #{isPetExperience}, #{address}, #{registerStatus}, #{volunteerApplyStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);
    @Update("<script>" +
            "UPDATE user " +
            "<set>" +
            "<if test='username != null'>username = #{username},</if>" +
            "<if test='password != null'>password = #{password},</if>" +
            "<if test='phone != null'>phone = #{phone},</if>" +
            "<if test='email != null'>email = #{email},</if>" +
            "<if test='role != null'>role = #{role},</if>" +
            "<if test='isVolunteer != null'>is_volunteer = #{isVolunteer},</if>" +
            "<if test='gender != null'>gender = #{gender},</if>" +
            "<if test='hasFixedIncome != null'>has_fixed_income = #{hasFixedIncome},</if>" +
            "<if test='birthYear != null'>birth_year = #{birthYear},</if>" +
            "<if test='isPetExperience != null'>is_pet_experience = #{isPetExperience},</if>" +
            "<if test='address != null'>address = #{address},</if>" +
            "<if test='registerStatus != null'>register_status = #{registerStatus},</if>" +
            "<if test='volunteerApplyStatus != null'>volunteer_apply_status = #{volunteerApplyStatus},</if>" +
            "</set>" +
            "WHERE user_id = #{userId}" +
            "</script>")
    int update(User user);

    @Delete("DELETE FROM user WHERE user_id = #{id}")
    int deleteById(@Param("id") Long id);

    // 根据志愿者申请状态查询
    @Select("SELECT * FROM user WHERE volunteer_apply_status = #{status}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "email", column = "email"),
            @Result(property = "role", column = "role"),
            @Result(property = "isVolunteer", column = "is_volunteer"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "hasFixedIncome", column = "has_fixed_income"),
            @Result(property = "birthYear", column = "birth_year"),
            @Result(property = "isPetExperience", column = "is_pet_experience"),
            @Result(property = "address", column = "address"),
            @Result(property = "registerStatus", column = "register_status"),
            @Result(property = "volunteerApplyStatus", column = "volunteer_apply_status")
    })
    List<User> findByVolunteerApplyStatus(@Param("status") String status);
}