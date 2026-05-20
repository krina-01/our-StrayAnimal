package com.springboot.service.Impl;
import com.springboot.entity.Animal;
import com.springboot.entity.User;
import com.springboot.repository.AnimalRepository;
import com.springboot.repository.UserRepository;
import com.springboot.service.AnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnimalServImpl  implements AnimalService {
    @Autowired
    AnimalRepository animalRepository;
    @Autowired
    UserRepository userRepository;
    @Override
    public ResponseEntity<?> insertAnimal(Animal animal) {
        try {
            // 业务逻辑：验证必要字段
            if (animal.getName() == null || animal.getName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "动物名称不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            if (animal.getSpecies() == null || animal.getSpecies().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                Map<String, String> errorMsg = new HashMap<>();
                errorMsg.put("message", "动物种类不能为空");
                return ResponseEntity.badRequest().body(errorMsg);
            }

            if (animal.getUserId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户ID不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            // 设置默认值
            if (animal.getGender() == null) {
                animal.setGender("unknown");
            }
            if (animal.getHealthStatus() == null) {
                animal.setHealthStatus("健康");
            }

            // 调用 Repository 插入数据
            int num = animalRepository.insertAnimal(animal);
            if(num == 0) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "添加失败");
                return ResponseEntity.internalServerError().body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "添加成功");
            response.put("animal", animal);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "添加失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }


    @Override
    public ResponseEntity<String> deleteById(Integer animalId, Long userId) {
        User foundUser = userRepository.findById(userId).orElse(null);
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
    public ResponseEntity<String> updateAdoptStatusById(String adopt_status,Integer animalId) {
        int num = 0;
        if ("available".equals(adopt_status)) {
            num = animalRepository.updateAdoptStatus("available", animalId);
        }
        else if ("adopted".equals(adopt_status)) {
            num = animalRepository.updateAdoptStatus("adopted", animalId);
        }
        if(num == 0) return ResponseEntity.internalServerError().body("更新失败,目标不存在...");
        return ResponseEntity.ok("更新成功...");
    }

    @Override
    public ResponseEntity<String> insertRescueRecord(String rescue,Integer animalId) {
        int num = animalRepository.addAnimalRescueRecord(rescue, animalId);
        if(num == 0) return ResponseEntity.internalServerError().body("添加失败,目标不存在...");
        return ResponseEntity.ok("添加成功...");
    }

    @Override
    public ResponseEntity<String> updateHealthStatus(String health_status, Integer animalId) {
        int num = animalRepository.updateHealthStatus(health_status, animalId);
        if(num == 0) return ResponseEntity.internalServerError().body("更新失败,目标不存在...");
        return ResponseEntity.ok("更新成功...");
    }


}
