package com.springboot.controller;

import com.springboot.entity.AdoptionVisit;
import com.springboot.repository.AdoptionVisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adoption-visits")
@CrossOrigin(origins = "http://localhost:5173")
public class AdoptionVisitController {

    @Autowired
    private AdoptionVisitRepository visitRepository;

    @GetMapping("/user/{userId}")
    public List<AdoptionVisit> getVisitsByUserId(@PathVariable Integer userId) {
        return visitRepository.findByUserId(userId);
    }

    @GetMapping("/surrender-user/{userId}")
    public List<AdoptionVisit> getVisitsBySurrenderUserId(@PathVariable Integer userId) {
        return visitRepository.findBySurrenderUserId(userId);
    }

    @GetMapping("/animal/{animalId}")
    public List<AdoptionVisit> getVisitsByAnimalId(@PathVariable Integer animalId) {
        return visitRepository.findByAnimalId(animalId);
    }

    @PostMapping
    public ResponseEntity<?> createVisit(@RequestBody AdoptionVisit visit) {
        visit.setVisitTime(LocalDateTime.now());
        visitRepository.insert(visit);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "回访记录提交成功");
        response.put("visit", visit);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVisit(@PathVariable Integer id,
                                         @RequestBody AdoptionVisit visit) {
        visit.setId(id);
        visit.setVisitTime(LocalDateTime.now());
        visitRepository.update(visit);

        Map<String, String> response = new HashMap<>();
        response.put("message", "回访记录更新成功");
        return ResponseEntity.ok(response);
    }
}