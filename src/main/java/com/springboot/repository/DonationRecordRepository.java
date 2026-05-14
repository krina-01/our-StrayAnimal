package com.springboot.repository;

import com.springboot.entity.DonationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DonationRecordRepository {

    @Select("SELECT * FROM donation_record")
    @Results(id = "donationRecordMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "fundraisingId", column = "fundraising_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "payTime", column = "pay_time"),
            @Result(property = "message", column = "message"),
            @Result(property = "isAnonymous", column = "is_anonymous"),
            @Result(property = "createTime", column = "create_time")
    })
    List<DonationRecord> findAll();

    @Select("SELECT * FROM donation_record WHERE id = #{id}")
    @ResultMap("donationRecordMap")
    Optional<DonationRecord> findById(@Param("id") Integer id);

    @Select("SELECT * FROM donation_record WHERE fundraising_id = #{fundraisingId}")
    @ResultMap("donationRecordMap")
    List<DonationRecord> findByFundraisingId(@Param("fundraisingId") Integer fundraisingId);

    @Select("SELECT * FROM donation_record WHERE user_id = #{userId}")
    @ResultMap("donationRecordMap")
    List<DonationRecord> findByUserId(@Param("userId") Integer userId);

    @Insert("INSERT INTO donation_record (fundraising_id, user_id, amount, pay_time, message, is_anonymous) " +
            "VALUES (#{fundraisingId}, #{userId}, #{amount}, #{payTime}, #{message}, #{isAnonymous})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DonationRecord record);

    // 多条件查询（供管理员使用）
    @Select("<script>" +
            "SELECT * FROM donation_record WHERE 1=1" +
            "<if test='fundraisingId != null'> AND fundraising_id = #{fundraisingId}</if>" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='startTime != null'> AND pay_time >= #{startTime}</if>" +
            "<if test='endTime != null'> AND pay_time &lt;= #{endTime}</if>" +
            "<if test='minAmount != null'> AND amount >= #{minAmount}</if>" +
            "<if test='maxAmount != null'> AND amount &lt;= #{maxAmount}</if>" +
            "</script>")
    @ResultMap("donationRecordMap")
    List<DonationRecord> search(@Param("fundraisingId") Integer fundraisingId,
                                @Param("userId") Integer userId,
                                @Param("startTime") String startTime,
                                @Param("endTime") String endTime,
                                @Param("minAmount") java.math.BigDecimal minAmount,
                                @Param("maxAmount") java.math.BigDecimal maxAmount);
}