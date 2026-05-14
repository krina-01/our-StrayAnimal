package com.springboot.controller;

import com.springboot.entity.*;
import com.springboot.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "http://localhost:5173")
public class AuditController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurrenderInfoRepository surrenderInfoRepository;

    @Autowired
    private AdoptionApplicationRepository adoptionApplicationRepository;

    @Autowired
    private DonationManageRepository donationManageRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private FundraisingRepository fundraisingRepository;

    // ==================== 送养审核 ====================

    @GetMapping("/surrender/pending")
    public List<SurrenderInfo> getPendingSurrenders() {
        return surrenderInfoRepository.findByAuditStatus("pending");
    }

    @GetMapping("/surrender/all")
    public List<SurrenderInfo> getAllSurrenders() {
        return surrenderInfoRepository.findAll();
    }

    @GetMapping("/surrender/user/{userId}")
    public List<SurrenderInfo> getUserSurrenders(@PathVariable Integer userId) {
        return surrenderInfoRepository.findByUserId(userId);
    }

    @PostMapping("/surrender/submit")
    public ResponseEntity<?> submitSurrender(@RequestBody SurrenderInfo surrenderInfo) {
        surrenderInfo.setSubmitTime(LocalDateTime.now());
        surrenderInfo.setAuditStatus("pending");
        surrenderInfoRepository.insert(surrenderInfo);

        Map<String, String> response = new HashMap<>();
        response.put("message", "送养申请已提交，等待管理员审核");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/surrender/{id}/approve")
    @Transactional
    public ResponseEntity<?> approveSurrender(@PathVariable Integer id) {
        Optional<SurrenderInfo> optionalSurrender = surrenderInfoRepository.findById(id);
        if (!optionalSurrender.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "送养申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        SurrenderInfo surrender = optionalSurrender.get();
        surrender.setAuditStatus("approved");
        surrender.setStatus("published");
        surrender.setAuditTime(LocalDateTime.now());
        surrenderInfoRepository.update(surrender);

        Animal animal = animalRepository.findById(surrender.getAnimalId());
        if (animal != null) {
            animalRepository.updateAnimalStatus(animal.getAnimalId(), "available");
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "送养申请审核通过");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/surrender/{id}/reject")
    public ResponseEntity<?> rejectSurrender(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> reason) {
        Optional<SurrenderInfo> optionalSurrender = surrenderInfoRepository.findById(id);
        if (!optionalSurrender.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "送养申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        SurrenderInfo surrender = optionalSurrender.get();
        surrender.setAuditStatus("rejected");
        surrender.setAuditTime(LocalDateTime.now());
        surrenderInfoRepository.update(surrender);

        Map<String, String> response = new HashMap<>();
        response.put("message", "送养申请审核拒绝");
        if (reason != null && reason.containsKey("reason")) {
            response.put("reason", reason.get("reason"));
        }
        return ResponseEntity.ok(response);
    }

    // ==================== 领养申请审核 ====================

    @GetMapping("/adoption/pending")
    public List<AdoptionApplication> getPendingAdoptions() {
        return adoptionApplicationRepository.findByAuditStatus("pending");
    }

    @GetMapping("/adoption/all")
    public List<AdoptionApplication> getAllAdoptions() {
        return adoptionApplicationRepository.findAll();
    }

    @GetMapping("/adoption/user/{userId}")
    public List<AdoptionApplication> getUserAdoptions(@PathVariable Integer userId) {
        return adoptionApplicationRepository.findByUserId(userId);
    }

    @PostMapping("/adoption/submit")
    public ResponseEntity<?> submitAdoption(@RequestBody AdoptionApplication adoptionApplication) {
        Optional<SurrenderInfo> optionalSurrender = surrenderInfoRepository.findById(adoptionApplication.getSurrenderId());
        if (!optionalSurrender.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "送养信息不存在");
            return ResponseEntity.status(404).body(error);
        }

        SurrenderInfo surrender = optionalSurrender.get();
        if (!"approved".equals(surrender.getAuditStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "该送养申请尚未通过审核");
            return ResponseEntity.status(400).body(error);
        }

        adoptionApplication.setApplicationTime(LocalDateTime.now());
        adoptionApplication.setAuditStatus("pending");
        adoptionApplicationRepository.insert(adoptionApplication);

        Map<String, String> response = new HashMap<>();
        response.put("message", "领养申请已提交，等待管理员审核");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/adoption/{id}/approve")
    @Transactional
    public ResponseEntity<?> approveAdoption(@PathVariable Integer id) {
        AdoptionApplication adoption = adoptionApplicationRepository.findById(id);
        if (adoption == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "领养申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        adoption.setAuditStatus("approved");
        adoption.setAuditTime(LocalDateTime.now());
        adoption.setAgreementStatus("effective");
        adoption.setAgreementSignTime(LocalDateTime.now());
        adoptionApplicationRepository.update(adoption);

        Optional<SurrenderInfo> optionalSurrender = surrenderInfoRepository.findById(adoption.getSurrenderId());
        if (optionalSurrender.isPresent()) {
            SurrenderInfo surrender = optionalSurrender.get();
            Animal animal = animalRepository.findById(surrender.getAnimalId());
            if (animal != null) {
                animalRepository.updateAnimalStatus(animal.getAnimalId(), "adopted");
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "领养申请审核通过");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/adoption/{id}/reject")
    public ResponseEntity<?> rejectAdoption(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> reason) {
        AdoptionApplication adoption = adoptionApplicationRepository.findById(id);
        if (adoption == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "领养申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        adoption.setAuditStatus("rejected");
        adoption.setAuditTime(LocalDateTime.now());
        adoptionApplicationRepository.update(adoption);

        Map<String, String> response = new HashMap<>();
        response.put("message", "领养申请审核拒绝");
        if (reason != null && reason.containsKey("reason")) {
            response.put("reason", reason.get("reason"));
        }
        return ResponseEntity.ok(response);
    }

    // ==================== 募捐资金提现审核 ====================

    @GetMapping("/donation/pending")
    public List<DonationManage> getPendingDonations() {
        return donationManageRepository.findByStatus(0);
    }

    @GetMapping("/donation/all")
    public List<DonationManage> getAllDonations() {
        return donationManageRepository.findAll();
    }

    @PostMapping("/donation/withdraw")
    public ResponseEntity<?> submitWithdrawal(@RequestBody DonationManage donationManage) {
        donationManage.setStatus(0);
        donationManage.setCreateTime(LocalDateTime.now());
        donationManageRepository.insert(donationManage);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "提现申请已提交，等待管理员审核");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/donation/{id}/approve")
    public ResponseEntity<?> approveDonation(@PathVariable Integer id, @RequestBody(required = false) Map<String, Object> publishData) {
        Optional<DonationManage> optionalDonation = donationManageRepository.findById(id);
        if (!optionalDonation.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "提现申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        DonationManage donation = optionalDonation.get();
        donation.setStatus(1);
        donation.setAuditTime(LocalDateTime.now());
        
        if (publishData != null) {
            if (publishData.containsKey("title")) {
                donation.setTitle((String) publishData.get("title"));
            }
            if (publishData.containsKey("content")) {
                donation.setContent((String) publishData.get("content"));
            }
        }
        
        donationManageRepository.update(donation);

        Map<String, String> response = new HashMap<>();
        response.put("message", "提现申请审核通过");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/donation/{id}/mark-paid")
    public ResponseEntity<?> markDonationAsPaid(@PathVariable Integer id) {
        Optional<DonationManage> optionalDonation = donationManageRepository.findById(id);
        if (!optionalDonation.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "提现申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        DonationManage donation = optionalDonation.get();
        donation.setStatus(3);
        donationManageRepository.update(donation);

        Map<String, String> response = new HashMap<>();
        response.put("message", "已标记为已打款");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/donation/{id}/reject")
    public ResponseEntity<?> rejectDonation(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> reason) {
        Optional<DonationManage> optionalDonation = donationManageRepository.findById(id);
        if (!optionalDonation.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "提现申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        DonationManage donation = optionalDonation.get();
        donation.setStatus(2);
        donation.setAuditTime(LocalDateTime.now());
        donationManageRepository.update(donation);

        Map<String, String> response = new HashMap<>();
        response.put("message", "提现申请审核拒绝");
        if (reason != null && reason.containsKey("reason")) {
            response.put("reason", reason.get("reason"));
        }
        return ResponseEntity.ok(response);
    }

    // ==================== 志愿者活动报名审核 ====================

    @GetMapping("/registration/pending")
    public ResponseEntity<?> getPendingRegistrations() {
        List<Map<String, Object>> pendingRegistrations = userActivityRepository.findPendingRegistrations();
        return ResponseEntity.ok(pendingRegistrations);
    }

    @PutMapping("/registration/{activityId}/approve")
    public ResponseEntity<?> approveRegistration(@PathVariable Integer activityId, @RequestParam Integer userId) {
        int rows = userActivityRepository.approveRegistration(userId, activityId);
        if (rows > 0) {
            return ResponseEntity.ok(Map.of("message", "报名审核通过"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "审核失败（可能记录不存在或状态不是 pending）"));
        }
    }

    @DeleteMapping("/registration/{activityId}/reject")
    public ResponseEntity<?> rejectRegistration(@PathVariable Integer activityId, @RequestParam Integer userId) {
        int rows = userActivityRepository.cancelRegistration(userId, activityId);
        if (rows > 0) {
            return ResponseEntity.ok(Map.of("message", "报名已拒绝"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "拒绝失败（可能记录不存在）"));
        }
    }

    // ==================== 领养人身份审核 ====================

    @GetMapping("/adopter/pending")
    public ResponseEntity<?> getPendingAdopters() {
        // 假设我们用一个特定的状态来标记待审核的领养人申请，或者查询 role='user' 但有申请记录的
        // 这里简化处理：如果 User 实体中有 registerStatus 或专门字段
        // 如果没有专门字段，通常是通过判断 role='user' 且提交过申请的用户
        // 为了统一，我们假设申请后 registerStatus 变为 'pending_adopter'
        List<User> pendingAdopters = userRepository.findByRegisterStatus("pending_adopter");
        return ResponseEntity.ok(pendingAdopters);
    }

    @PutMapping("/adopter/{id}/approve")
    public ResponseEntity<?> approveAdopter(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }
        User user = optionalUser.get();
        user.setRole("adopter"); // 提升为领养人
        user.setRegisterStatus("approved"); // 清除待审核状态
        userRepository.update(user);
        return ResponseEntity.ok(Map.of("message", "已批准为领养人"));
    }

    @PutMapping("/adopter/{id}/reject")
    public ResponseEntity<?> rejectAdopter(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }
        User user = optionalUser.get();
        user.setRegisterStatus("rejected"); 
        userRepository.update(user);
        return ResponseEntity.ok(Map.of("message", "已拒绝领养人申请"));
    }

    // ==================== 募捐活动审核 ====================

    @GetMapping("/fundraising/pending")
    public List<Fundraising> getPendingFundraisings() {
        return fundraisingRepository.findByStatus("pending");
    }

    @GetMapping("/fundraising/all")
    public List<Fundraising> getAllFundraisings() {
        return fundraisingRepository.findAll();
    }

    @GetMapping("/fundraising/user/{userId}")
    public List<Fundraising> getUserFundraisings(@PathVariable Integer userId) {
        return fundraisingRepository.findByCreatorId(userId);
    }

    @PutMapping("/fundraising/{id}/approve")
    @Transactional
    public ResponseEntity<?> approveFundraising(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> auditData) {
        Optional<Fundraising> optionalFundraising = fundraisingRepository.findById(id);
        if (!optionalFundraising.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "募捐活动不存在");
            return ResponseEntity.status(404).body(error);
        }

        Fundraising fundraising = optionalFundraising.get();
        fundraising.setStatus("ongoing");
        fundraising.setUpdateTime(LocalDateTime.now());
        fundraisingRepository.update(fundraising);

        Map<String, String> response = new HashMap<>();
        response.put("message", "募捐活动审核通过，已发布");
        if (auditData != null && auditData.containsKey("remark")) {
            response.put("remark", auditData.get("remark"));
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/fundraising/{id}/reject")
    @Transactional
    public ResponseEntity<?> rejectFundraising(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> auditData) {
        Optional<Fundraising> optionalFundraising = fundraisingRepository.findById(id);
        if (!optionalFundraising.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "募捐活动不存在");
            return ResponseEntity.status(404).body(error);
        }

        Fundraising fundraising = optionalFundraising.get();
        fundraising.setStatus("terminated");
        fundraising.setUpdateTime(LocalDateTime.now());
        fundraisingRepository.update(fundraising);

        Map<String, String> response = new HashMap<>();
        response.put("message", "募捐活动审核拒绝");
        if (auditData != null && auditData.containsKey("remark")) {
            response.put("remark", auditData.get("remark"));
        }
        return ResponseEntity.ok(response);
    }

    // ==================== 综合审核统计 ====================

    @GetMapping("/statistics")
    public ResponseEntity<?> getAuditStatistics() {
        List<SurrenderInfo> pendingSurrenders = surrenderInfoRepository.findByAuditStatus("pending");
        List<AdoptionApplication> pendingAdoptions = adoptionApplicationRepository.findByAuditStatus("pending");
        List<DonationManage> pendingDonations = donationManageRepository.findByStatus(0);
        List<User> pendingUsers = userRepository.findByRegisterStatus("pending");
        List<User> pendingVolunteers = userRepository.findByRegisterStatus("pending_volunteer");
        List<User> pendingDeletes = userRepository.findByRegisterStatus("pending_delete");
        List<Map<String, Object>> pendingRegistrations = userActivityRepository.findPendingRegistrations();
        List<Fundraising> pendingFundraisings = fundraisingRepository.findByStatus("pending");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("pendingSurrenders", pendingSurrenders.size());
        statistics.put("pendingAdoptions", pendingAdoptions.size());
        statistics.put("pendingDonations", pendingDonations.size());
        statistics.put("pendingFundraisings", pendingFundraisings.size());
        statistics.put("pendingUsers", pendingUsers.size());
        statistics.put("pendingVolunteers", pendingVolunteers.size());
        statistics.put("pendingDeletes", pendingDeletes.size());
        statistics.put("pendingRegistrations", pendingRegistrations.size());
        statistics.put("total", pendingSurrenders.size() + pendingAdoptions.size() + 
                                  pendingDonations.size() + pendingFundraisings.size() +
                                  pendingUsers.size() + pendingVolunteers.size() + 
                                  pendingDeletes.size() + pendingRegistrations.size());

        return ResponseEntity.ok(statistics);
    }
}
