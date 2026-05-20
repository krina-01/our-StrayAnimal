package com.springboot.service;

import com.springboot.entity.Animal;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface AnimalService {
    ResponseEntity<?> insertAnimal(Animal  animal);
    ResponseEntity<String> deleteById(Integer animalId,Long userId);
    ResponseEntity<String> updateById(Animal animal);
    ResponseEntity<Animal> selectById(Integer animalId);
    ResponseEntity<List<Animal>> selectAll();
    ResponseEntity<String> updateAdoptStatusById(String adopt_status,Integer animalId);
    ResponseEntity<String> insertRescueRecord(String rescue,Integer animalId);
    ResponseEntity<String> updateHealthStatus(String health_status,Integer animalId);
}
