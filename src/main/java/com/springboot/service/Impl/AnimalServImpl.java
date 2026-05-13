package com.springboot.service.Impl;
import com.springboot.entity.Animal;
import com.springboot.entity.User;
import com.springboot.repository.AnimalRepository;
import com.springboot.repository.UserRepository;
import com.springboot.service.AnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimalServImpl  implements AnimalService {
    @Autowired
    AnimalRepository animalRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public ResponseEntity<String> insertAnimal(Animal animal) {
        int num = animalRepository.insertAnimal(animal);
        if(num == 0) return ResponseEntity.internalServerError().body("添加失败...");
        return ResponseEntity.ok("添加成功...");
    }

    @Override
    public ResponseEntity<String> deleteById(Integer animalId, Long userId) {
        User foundUser = userRepository.findById(userId).orElse(null);;
        if(!"admin".equals(foundUser.getRole())) return ResponseEntity.internalServerError().body("无权访问，仅管理员可删除...");
        int num = animalRepository.deleteById(animalId);
        if(num == 0) return ResponseEntity.internalServerError().body("删除失败,目标不存在...");
        return ResponseEntity.ok("删除成功...");
    }

    @Override
    public ResponseEntity<String> updateById(Animal animal) {
        int num = animalRepository.updateById(animal);
        if(num == 0) return ResponseEntity.internalServerError().body("修改失败");
        return ResponseEntity.ok("修改成功");
    }

    @Override
    public ResponseEntity<Animal> selectById(Integer animalId) {
        Animal animal = animalRepository.selectById(animalId);
        if (animal == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(animal);
    }

    @Override
    public ResponseEntity<List<Animal>> selectAll() {
        List< Animal> animals = animalRepository.selectAll();
        if (animals.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(animals);
    }

    @Override
    public ResponseEntity<String> updateAdoptStatusById() {
        return null;
    }
}
