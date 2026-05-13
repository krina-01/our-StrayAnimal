package com.springboot.service;

import com.springboot.entity.Animal;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AnimalService {
    ResponseEntity<String> insertAnimal(Animal animal);
    ResponseEntity<String> deleteById(Integer animalId,Long userId);
    ResponseEntity<String> updateById(Animal animal);
    ResponseEntity<Animal> selectById(Integer animalId);
    ResponseEntity<List<Animal>> selectAll();
    ResponseEntity<String> updateAdoptStatusById();
}
