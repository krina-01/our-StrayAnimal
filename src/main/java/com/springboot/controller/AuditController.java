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
        surrender.setAuditTime(LocalDateTime.now());
        surrenderInfoRepository.update(surrender);

        Optional<Animal> optionalAnimal = animalRepository.findById(surrender.getAnimalId());
        if (optionalAnimal.isPresent()) {
            Animal animal = optionalAnimal.get();
            animal.setAdoptStatus("available");
            animalRepository.update(animal);
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
        Optional<AdoptionApplication> optionalAdoption = adoptionApplicationRepository.findById(id);
        if (!optionalAdoption.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "领养申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        AdoptionApplication adoption = optionalAdoption.get();
        adoption.setAuditStatus("approved");
        adoption.setAuditTime(LocalDateTime.now());
        adoption.setAgreementStatus("effective");
        adoption.setAgreementSignTime(LocalDateTime.now());
        adoptionApplicationRepository.update(adoption);

        Optional<SurrenderInfo> optionalSurrender = surrenderInfoRepository.findById(adoption.getSurrenderId());
        if (optionalSurrender.isPresent()) {
            SurrenderInfo surrender = optionalSurrender.get();
            Optional<Animal> optionalAnimal = animalRepository.findById(surrender.getAnimalId());
            if (optionalAnimal.isPresent()) {
                Animal animal = optionalAnimal.get();
                animal.setAdoptStatus("adopted");
                animalRepository.update(animal);
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "领养申请审核通过");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/adoption/{id}/reject")
    public ResponseEntity<?> rejectAdoption(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> reason) {
        Optional<AdoptionApplication> optionalAdoption = adoptionApplicationRepository.findById(id);
        if (!optionalAdoption.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "领养申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        AdoptionApplication adoption = optionalAdoption.get();
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

    // ==================== 综合审核统计 ====================

    @GetMapping("/statistics")
    public ResponseEntity<?> getAuditStatistics() {
        List<SurrenderInfo> pendingSurrenders = surrenderInfoRepository.findByAuditStatus("pending");
        List<AdoptionApplication> pendingAdoptions = adoptionApplicationRepository.findByAuditStatus("pending");
        List<DonationManage> pendingDonations = donationManageRepository.findByStatus(0);
        List<User> pendingUsers = userRepository.findByRegisterStatus("pending");
        List<User> pendingVolunteers = userRepository.findByRegisterStatus("pending_volunteer");
        List<User> pendingDeletes = userRepository.findByRegisterStatus("pending_delete");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("pendingSurrenders", pendingSurrenders.size());
        statistics.put("pendingAdoptions", pendingAdoptions.size());
        statistics.put("pendingDonations", pendingDonations.size());
        statistics.put("pendingUsers", pendingUsers.size());
        statistics.put("pendingVolunteers", pendingVolunteers.size());
        statistics.put("pendingDeletes", pendingDeletes.size());
        statistics.put("total", pendingSurrenders.size() + pendingAdoptions.size() + 
                                  pendingDonations.size() + pendingUsers.size() + 
                                  pendingVolunteers.size() + pendingDeletes.size());

        return ResponseEntity.ok(statistics);
    }
}
