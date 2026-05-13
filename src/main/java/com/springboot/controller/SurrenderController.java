package com.springboot.controller;

import com.springboot.entity.Animal;
import com.springboot.entity.SurrenderInfo;
import com.springboot.repository.AnimalRepository;
import com.springboot.repository.SurrenderInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/surrender")
@CrossOrigin(origins = "http://localhost:5173")
public class SurrenderController {

    @Autowired
    private SurrenderInfoRepository surrenderInfoRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getSurrenderListByUser(@PathVariable Integer userId) {
        List<SurrenderInfo> surrenderList = surrenderInfoRepository.findByUserId(userId);

        for (SurrenderInfo surrender : surrenderList) {
            Animal animal = animalRepository.findById(surrender.getAnimalId());
            surrender.setAnimal(animal);
        }

        return ResponseEntity.ok(surrenderList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSurrenderById(@PathVariable Integer id) {
        SurrenderInfo surrender = surrenderInfoRepository.findById(id);
        
        if (surrender == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "送养信息不存在");
            return ResponseEntity.status(404).body(error);
        }

        Animal animal = animalRepository.findById(surrender.getAnimalId());
        surrender.setAnimal(animal);

        return ResponseEntity.ok(surrender);
    }

    @GetMapping("/animal/{animalId}")
    public ResponseEntity<?> getSurrenderByAnimalId(@PathVariable Integer animalId) {
        List<SurrenderInfo> surrenderList = surrenderInfoRepository.findByAnimalId(animalId);

        for (SurrenderInfo surrender : surrenderList) {
            Animal animal = animalRepository.findById(surrender.getAnimalId());
            surrender.setAnimal(animal);
        }

        return ResponseEntity.ok(surrenderList);
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publishSurrender(@RequestBody Map<String, Object> requestData) {
        Integer userId = (Integer) requestData.get("userId");
        Integer animalId = (Integer) requestData.get("animalId");
        String surrenderReason = (String) requestData.get("surrenderReason");

        Animal animal = animalRepository.findById(animalId);
        if (animal == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "动物信息不存在");
            return ResponseEntity.status(404).body(error);
        }

        SurrenderInfo existingSurrender = (SurrenderInfo) surrenderInfoRepository.findByAnimalId(animalId);
        if (existingSurrender != null && !"rejected".equals(existingSurrender.getAuditStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "该动物已经有送养申请");
            return ResponseEntity.status(400).body(error);
        }

        SurrenderInfo surrenderInfo = new SurrenderInfo();
        surrenderInfo.setUserId(userId);
        surrenderInfo.setAnimalId(animalId);
        surrenderInfo.setSurrenderReason(surrenderReason);
        surrenderInfo.setSubmitTime(LocalDateTime.now());
        surrenderInfo.setAuditStatus("pending");

        surrenderInfoRepository.insert(surrenderInfo);

        return ResponseEntity.ok(surrenderInfo);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveSurrender(@PathVariable Integer id) {
        SurrenderInfo surrenderInfo = surrenderInfoRepository.findById(id);
        if (surrenderInfo == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "送养信息不存在");
            return ResponseEntity.status(404).body(error);
        }

        surrenderInfo.setAuditStatus("approved");
        surrenderInfo.setAuditTime(LocalDateTime.now());
        surrenderInfo.setStatus("published");
        surrenderInfoRepository.update(surrenderInfo);

        Map<String, String> response = new HashMap<>();
        response.put("message", "审核通过");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectSurrender(@PathVariable Integer id) {
        SurrenderInfo surrenderInfo = surrenderInfoRepository.findById(id);
        if (surrenderInfo == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "送养信息不存在");
            return ResponseEntity.status(404).body(error);
        }

        surrenderInfo.setAuditStatus("rejected");
        surrenderInfo.setAuditTime(LocalDateTime.now());
        surrenderInfoRepository.update(surrenderInfo);

        Map<String, String> response = new HashMap<>();
        response.put("message", "审核拒绝");
        return ResponseEntity.ok(response);
    }
}
