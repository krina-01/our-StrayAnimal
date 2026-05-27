package com.springboot.controller;

import com.springboot.entity.Animal;
import com.springboot.repository.AnimalRepository;
import com.springboot.service.AnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/animals")
@CrossOrigin(origins = "http://localhost:5173")
public class AnimalController {

    @Autowired
    AnimalRepository animalRepository;

    @Autowired
    private AnimalService animalService;

    @PostMapping("/add")
    public ResponseEntity<?> addAnimal(@RequestBody Animal animal) {
        return animalService.insertAnimal(animal);
    }
    // 删除动物
    @DeleteMapping("/delete/{animalId}")
    public ResponseEntity<String> deleteAnimal(@PathVariable Integer animalId, @RequestParam Long userId) {
        return animalService.deleteById(animalId, userId);
    }
    // 更新动物
    @PutMapping("/update/{animalId}")
    public ResponseEntity<String> updateAnimal(@PathVariable Integer animalId, @RequestBody Animal animal) {
        animal.setAnimalId(animalId);
        return animalService.updateById(animal);
    }
    // 更新动物领养状态
    @PostMapping("/adopt-status/{animalId}")
    public ResponseEntity<String> updateAdoptStatus(@PathVariable Integer animalId, @RequestParam String adoptStatus) {
        return animalService.updateAdoptStatusById(adoptStatus, animalId);
    }
    // 添加动物救助记录
    @PostMapping("/rescue-record/{animalId}")
    public ResponseEntity<String> addRescueRecord(@PathVariable Integer animalId, @RequestParam String rescueRecord) {
        return animalService.insertRescueRecord(rescueRecord, animalId);
    }
    // 更新动物健康状态
    @PostMapping("/health-status/{animalId}")
    public ResponseEntity<String> updateHealthStatus(@PathVariable Integer animalId, @RequestParam String healthStatus) {
        return animalService.updateHealthStatus(healthStatus, animalId);
    }
//    @PostMapping("/ai/ask")
//    public String askAI(@RequestParam String question) {
//        // 1. 构建请求
//        String prompt = "你是一个流浪动物救助专家，请回答：" + question;
//
//        // 2. 调用智谱AI接口
//        // 3. 返回回答给前端
//    }

    @GetMapping
    public List<Animal> getAllAnimals() {
        List<Animal> animals = animalRepository.findAll();
        System.out.println("=== 所有动物列表 ===");
        for (Animal animal : animals) {
            System.out.println("ID: " + animal.getAnimalId() + ", 名字: " + animal.getName() + ", 状态: " + animal.getAdoptStatus());
        }
        return animals;
    }

    //
    @GetMapping("/available")
    public List<Animal> getAvailableAnimals() {
        List<Animal> animals = animalRepository.findAvailableAnimals();
        System.out.println("=== 可领养动物列表 ===");
        for (Animal animal : animals) {
            System.out.println("ID: " + animal.getAnimalId() + ", 名字: " + animal.getName() + ", 状态: " + animal.getAdoptStatus());
        }
        return animals;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAnimalById(@PathVariable Integer id) {
        Animal animal = animalRepository.findById(id);
        if (animal != null) {
            return ResponseEntity.ok(animal);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "动物信息不存在");
            return ResponseEntity.status(404).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public List<Animal> getAnimalsByUserId(@PathVariable Integer userId) {
        return animalRepository.findByUserId(userId);
    }
}