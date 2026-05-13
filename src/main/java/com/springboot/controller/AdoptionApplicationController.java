package com.springboot.controller;

import com.springboot.entity.AdoptionApplication;
import com.springboot.entity.Animal;
import com.springboot.repository.AdoptionApplicationRepository;
import com.springboot.repository.AnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adoption-applications")
@CrossOrigin(origins = "http://localhost:5173")
public class AdoptionApplicationController {

    @Autowired
    private AdoptionApplicationRepository applicationRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @GetMapping
    public List<AdoptionApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationById(@PathVariable Integer id) {
        AdoptionApplication application = applicationRepository.findById(id);
        if (application != null) {
            return ResponseEntity.ok(application);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "申请不存在");
            return ResponseEntity.status(404).body(error);
        }
    }

    @GetMapping("/status/{status}")
    public List<AdoptionApplication> getApplicationsByStatus(@PathVariable String status) {
        return applicationRepository.findByAuditStatus(status);
    }

    @GetMapping("/user/{userId}")
    public List<AdoptionApplication> getApplicationsByUserId(@PathVariable Integer userId) {
        return applicationRepository.findByUserId(userId);
    }

    @GetMapping("/surrender/{surrenderId}")
    public List<AdoptionApplication> getApplicationsBySurrenderId(@PathVariable Integer surrenderId) {
        return applicationRepository.findBySurrenderId(surrenderId);
    }

    @PostMapping
    public ResponseEntity<?> createApplication(@RequestBody AdoptionApplication application) {
        application.setAuditStatus("pending");
        application.setApplicationTime(LocalDateTime.now());

        applicationRepository.insert(application);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "申请提交成功");
        response.put("application", application);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveApplication(@PathVariable Integer id) {
        AdoptionApplication application = applicationRepository.findById(id);
        if (application == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        application.setAuditStatus("approved");
        application.setAuditTime(LocalDateTime.now());
        applicationRepository.update(application);

        Map<String, String> response = new HashMap<>();
        response.put("message", "申请已通过");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectApplication(@PathVariable Integer id) {
        AdoptionApplication application = applicationRepository.findById(id);
        if (application == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        application.setAuditStatus("rejected");
        application.setAuditTime(LocalDateTime.now());
        applicationRepository.update(application);

        Map<String, String> response = new HashMap<>();
        response.put("message", "申请已拒绝");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/sign-agreement")
    public ResponseEntity<?> signAgreement(@PathVariable Integer id,
                                           @RequestBody Map<String, String> requestData) {
        AdoptionApplication application = applicationRepository.findById(id);
        if (application == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        application.setAgreementSignTime(LocalDateTime.now());
        application.setAgreementContent(requestData.get("agreementContent"));
        application.setAgreementStatus("effective");
        applicationRepository.update(application);

        // 更新动物的领养状态为 adopted
        if (application.getSurrender() != null && application.getSurrender().getAnimalId() != null) {
            Animal animal = animalRepository.findById(application.getSurrender().getAnimalId());
            if (animal != null) {
                animal.setAdoptStatus("adopted");
                animalRepository.updateAnimalStatus(animal.getAnimalId(), "adopted");
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "协议签署成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Integer id) {
        AdoptionApplication application = applicationRepository.findById(id);
        if (application == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "申请不存在");
            return ResponseEntity.status(404).body(error);
        }

        applicationRepository.deleteById(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "申请已删除");
        return ResponseEntity.ok(response);
    }
}

