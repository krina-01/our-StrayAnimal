package com.springboot.controller;

import com.springboot.entity.Animal;
import com.springboot.repository.AnimalRepository;
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
    private AnimalRepository animalRepository;

    @GetMapping
    public List<Animal> getAllAnimals() {
        List<Animal> animals = animalRepository.findAll();
        System.out.println("=== 所有动物列表 ===");
        for (Animal animal : animals) {
            System.out.println("ID: " + animal.getAnimalId() + ", 名字: " + animal.getName() + ", 状态: " + animal.getAdoptStatus());
        }
        return animals;
    }

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