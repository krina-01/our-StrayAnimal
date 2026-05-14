package com.springboot.controller;

import com.springboot.entity.Animal;
import com.springboot.entity.Fundraising;
import com.springboot.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminStatisticsController {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurrenderInfoRepository surrenderInfoRepository;

    @Autowired
    private AdoptionApplicationRepository adoptionApplicationRepository;

    @Autowired
    private FundraisingRepository fundraisingRepository;

    @Autowired
    private DonationRecordRepository donationRecordRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @GetMapping("/statistics/adoption")
    public ResponseEntity<?> getAdoptionStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 救助数量(已审核通过的送养)
        long rescueCount = surrenderInfoRepository.findByAuditStatus("approved").size();
        statistics.put("rescueCount", rescueCount);

        // 领养申请量
        long adoptionApplicationCount = adoptionApplicationRepository.findAll().size();
        statistics.put("adoptionApplicationCount", adoptionApplicationCount);

        // 领养成功数量(已审核通过的领养申请)
        long successfulAdoptions = adoptionApplicationRepository.findByAuditStatus("approved").size();
        statistics.put("successfulAdoptions", successfulAdoptions);

        // 领养成功率
        double successRate = adoptionApplicationCount > 0 ?
                (double) successfulAdoptions / adoptionApplicationCount * 100 : 0;
        statistics.put("successRate", String.format("%.2f%%", successRate));

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/basic")
    public ResponseEntity<?> getBasicStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 动物总数
        long totalAnimals = animalRepository.findAll().size();
        statistics.put("totalAnimals", totalAnimals);

        // 用户总数
        long totalUsers = userRepository.findAll().size();
        statistics.put("totalUsers", totalUsers);

        // 救助站总数(这里用送养信息数量表示)
        long totalSurrenders = surrenderInfoRepository.findAll().size();
        statistics.put("totalRescueStations", totalSurrenders);

        // 信息发布总数(送养+领养申请+募捐活动)
        long totalAdoptions = adoptionApplicationRepository.findAll().size();
        long totalFundraisings = fundraisingRepository.findAll().size();
        long totalPublications = totalSurrenders + totalAdoptions + totalFundraisings;
        statistics.put("totalPublications", totalPublications);

        // 新增：审核状态统计（用于图表）
        Map<String, Object> auditCount = new HashMap<>();
        
        // 用户注册审核统计（使用 registerStatus 字段）
        long userPending = userRepository.findByRegisterStatus("pending").size();
        long userPass = userRepository.findByRegisterStatus("approved").size();
        long userReject = userRepository.findByRegisterStatus("rejected").size();
        auditCount.put("userPending", userPending);
        auditCount.put("userPass", userPass);
        auditCount.put("userReject", userReject);
        
        // 志愿者申请审核统计
        long volPending = userRepository.findByVolunteerApplyStatus("pending").size();
        long volPass = userRepository.findByVolunteerApplyStatus("approved").size();
        long volReject = userRepository.findByVolunteerApplyStatus("rejected").size();
        auditCount.put("volPending", volPending);
        auditCount.put("volPass", volPass);
        auditCount.put("volReject", volReject);
        
        // 领养申请审核统计
        long adoptPending = adoptionApplicationRepository.findByAuditStatus("pending").size();
        long adoptPass = adoptionApplicationRepository.findByAuditStatus("approved").size();
        long adoptReject = adoptionApplicationRepository.findByAuditStatus("rejected").size();
        auditCount.put("adoptPending", adoptPending);
        auditCount.put("adoptPass", adoptPass);
        auditCount.put("adoptReject", adoptReject);
        
        // 送养申请审核统计
        long surrPending = surrenderInfoRepository.findByAuditStatus("pending").size();
        long surrPass = surrenderInfoRepository.findByAuditStatus("approved").size();
        long surrReject = surrenderInfoRepository.findByAuditStatus("rejected").size();
        auditCount.put("surrPending", surrPending);
        auditCount.put("surrPass", surrPass);
        auditCount.put("surrReject", surrReject);
        
        // 活动报名审核统计
        long activityPending = userActivityRepository.findByStatus("pending").size();
        long activityPass = userActivityRepository.findByStatus("approved").size();
        long activityReject = 0L; // user_activity没有rejected状态，拒绝的记录会被删除
        auditCount.put("activityPending", activityPending);
        auditCount.put("activityPass", activityPass);
        auditCount.put("activityReject", activityReject);
        
        // 募捐活动审核统计
        long fundraisingPending = fundraisingRepository.findByStatus("pending").size();
        long fundraisingPass = fundraisingRepository.findByStatus("ongoing").size();
        long fundraisingReject = fundraisingRepository.findByStatus("terminated").size();
        auditCount.put("fundraisingPending", fundraisingPending);
        auditCount.put("fundraisingPass", fundraisingPass);
        auditCount.put("fundraisingReject", fundraisingReject);
        
        statistics.put("auditCount", auditCount);

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/fundraising")
    public ResponseEntity<?> getFundraisingStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 募捐项目数
        long totalFundraisings = fundraisingRepository.findAll().size();
        statistics.put("totalFundraisings", totalFundraisings);

        // 进行中的募捐项目数
        long ongoingFundraisings = fundraisingRepository.findByStatus("ongoing").size();
        statistics.put("ongoingFundraisings", ongoingFundraisings);

        // 总募捐金额
        List<Fundraising> allFundraisings = fundraisingRepository.findAll();
        BigDecimal totalAmount = allFundraisings.stream()
                .map(f -> f.getCurrentAmount() != null ? f.getCurrentAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.put("totalAmount", totalAmount);

        // 目标金额总和
        BigDecimal targetAmount = allFundraisings.stream()
                .map(f -> f.getTargetAmount() != null ? f.getTargetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.put("targetAmount", targetAmount);

        // 捐款人次(这里简化处理,实际应该有专门的捐款记录表)
        long donationCount = donationRecordRepository.findAll().size();
        statistics.put("donationCount", donationCount);

        // 资金使用情况(已完成的项目金额)
        List<Fundraising> completedFundraisings = fundraisingRepository.findByStatus("completed");
        BigDecimal usedAmount = completedFundraisings.stream()
                .map(f -> f.getCurrentAmount() != null ? f.getCurrentAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.put("usedAmount", usedAmount);

        // 新增：募捐项目状态分布（用于图表）
        Map<String, Long> fundraisingStatus = new HashMap<>();
        long pendingCount = fundraisingRepository.findByStatus("pending").size();
        long ongoingCount = fundraisingRepository.findByStatus("ongoing").size();
        long completedCount = fundraisingRepository.findByStatus("completed").size();
        long terminatedCount = fundraisingRepository.findByStatus("terminated").size();
        
        fundraisingStatus.put("pending", pendingCount);
        fundraisingStatus.put("ongoing", ongoingCount);
        fundraisingStatus.put("finished", completedCount);  // 前端期望 finished
        fundraisingStatus.put("terminated", terminatedCount);
        statistics.put("fundraisingStatus", fundraisingStatus);

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/animal-distribution")
    public ResponseEntity<?> getAnimalDistribution() {
        Map<String, Object> statistics = new HashMap<>();

        List<Animal> animals = animalRepository.findAll();

        // 宠物种类分布 - 转换为 ECharts 需要的数组格式
        Map<String, Long> speciesMap = new HashMap<>();
        for (Animal animal : animals) {
            String species = animal.getSpecies() != null ? animal.getSpecies() : "未知";
            speciesMap.put(species,
                    speciesMap.getOrDefault(species, 0L) + 1);
        }
        
        // 转换为 [{name: '猫', value: 5}, ...] 格式
        List<Map<String, Object>> speciesList = new java.util.ArrayList<>();
        for (Map.Entry<String, Long> entry : speciesMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            speciesList.add(item);
        }
        statistics.put("speciesList", speciesList);

        // 区域分布 - 转换为 ECharts 需要的数组格式
        Map<String, Long> locationMap = new HashMap<>();
        for (Animal animal : animals) {
            String location = animal.getLocation() != null ? animal.getLocation() : "未知";
            locationMap.put(location,
                    locationMap.getOrDefault(location, 0L) + 1);
        }
        
        List<Map<String, Object>> areaList = new java.util.ArrayList<>();
        for (Map.Entry<String, Long> entry : locationMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            areaList.add(item);
        }
        statistics.put("areaList", areaList);

        // 救助状态分布 - 转换为 ECharts 需要的数组格式
        Map<String, Long> statusMap = new HashMap<>();
        for (Animal animal : animals) {
            String status = animal.getAdoptStatus() != null ? animal.getAdoptStatus() : "unknown";
            statusMap.put(status,
                    statusMap.getOrDefault(status, 0L) + 1);
        }
        
        List<Map<String, Object>> statusList = new java.util.ArrayList<>();
        for (Map.Entry<String, Long> entry : statusMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            statusList.add(item);
        }
        statistics.put("statusList", statusList);

        return ResponseEntity.ok(statistics);
    }
}
