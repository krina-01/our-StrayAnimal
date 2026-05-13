package com.springboot.controller;


import com.springboot.entity.User;
import com.springboot.repository.UserRepository;
import com.springboot.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id.longValue());
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户名已存在");
            return ResponseEntity.status(400).body(error);
        }

        String encodedPassword = PasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRegisterStatus("pending");
        user.setRole("user");
        if (user.getIsVolunteer() == null) {
            user.setIsVolunteer(false);
        }

        userRepository.insert(user);
        User savedUser = userRepository.findById(user.getUserId().longValue()).get();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "注册成功,等待管理员审核");
        response.put("user", savedUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(error);
        }

        User foundUser = user.get();
        if (!PasswordEncoder.matches(password, foundUser.getPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(error);
        }

        if ("pending".equals(foundUser.getRegisterStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "账号正在审核中，请耐心等待");
            return ResponseEntity.status(403).body(error);
        }

        if ("rejected".equals(foundUser.getRegisterStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "账号审核未通过");
            return ResponseEntity.status(403).body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "登录成功");
        response.put("user", foundUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();

        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            String encodedPassword = PasswordEncoder.encode(userDetails.getPassword());
            user.setPassword(encodedPassword);
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getGender() != null) {
            user.setGender(userDetails.getGender());
        }
        if (userDetails.getHasFixedIncome() != null) {
            user.setHasFixedIncome(userDetails.getHasFixedIncome());
        }
        if (userDetails.getBirthYear() != null) {
            user.setBirthYear(userDetails.getBirthYear());
        }
        if (userDetails.getIsPetExperience() != null) {
            user.setIsPetExperience(userDetails.getIsPetExperience());
        }
        if (userDetails.getAddress() != null) {
            user.setAddress(userDetails.getAddress());
        }

        userRepository.update(user);
        User updatedUser = userRepository.findById(user.getUserId().longValue()).get();
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id.longValue());
        if (!user.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User userToDelete = user.get();
        userToDelete.setRegisterStatus("pending_delete");
        userRepository.update(userToDelete);

        Map<String, String> response = new HashMap<>();
        response.put("message", "注销申请已提交,等待管理员审核");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }
    }

    @GetMapping("/role/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        return userRepository.findByRole(role);
    }

    @GetMapping("/volunteers")
    public List<User> getVolunteers() {
        return userRepository.findByIsVolunteer(true);
    }

    @GetMapping("/pending")
    public List<User> getPendingUsers() {
        return userRepository.findByRegisterStatus("pending");
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();
        user.setRegisterStatus("approved");
        userRepository.update(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "审核通过");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();
        user.setRegisterStatus("rejected");
        userRepository.update(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "审核拒绝");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/assign-role")
    public ResponseEntity<?> assignRole(@PathVariable Integer id, @RequestBody Map<String, String> requestData) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();
        String role = requestData.get("role");
        
        if ("admin".equals(role)) {
            user.setRole("admin");
        } else if ("volunteer".equals(role)) {
            user.setIsVolunteer(true);
        }
        
        userRepository.update(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "权限分配成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/remove-role")
    public ResponseEntity<?> removeRole(@PathVariable Integer id, @RequestBody Map<String, String> requestData) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();
        String role = requestData.get("role");
        
        if ("volunteer".equals(role)) {
            user.setIsVolunteer(false);
        }
        
        userRepository.update(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "权限移除成功");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/apply-volunteer")
    public ResponseEntity<?> applyVolunteer(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "志愿者申请已提交，等待管理员审核");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve-volunteer")
    public ResponseEntity<?> approveVolunteer(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();
        user.setIsVolunteer(true);
        userRepository.update(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "志愿者申请已通过");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel-volunteer")
    public ResponseEntity<?> cancelVolunteer(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();
        user.setIsVolunteer(false);
        userRepository.update(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "已取消志愿者身份");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/apply-adopter")
    public ResponseEntity<?> applyAdopter(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "领养人申请已提交，等待管理员审核");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve-adopter")
    public ResponseEntity<?> approveAdopter(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();
        user.setRole("adopter");
        userRepository.update(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "领养人申请已通过");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel-adopter")
    public ResponseEntity<?> cancelAdopter(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();
        if ("adopter".equals(user.getRole())) {
            user.setRole("user");
        }
        userRepository.update(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "已取消领养人身份");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-delete")
    public List<User> getPendingDeleteUsers() {
        return userRepository.findByRegisterStatus("pending_delete");
    }

    @PutMapping("/{id}/approve-delete")
    public ResponseEntity<?> approveDelete(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        userRepository.deleteById(id.longValue());

        Map<String, String> response = new HashMap<>();
        response.put("message", "注销申请已通过,账户已删除");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject-delete")
    public ResponseEntity<?> rejectDelete(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id.longValue());
        if (!optionalUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);
        }

        User user = optionalUser.get();
        user.setRegisterStatus("approved");
        userRepository.update(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "注销申请已拒绝");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/verify")
    public ResponseEntity<?> verifyAdmin(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(error);
        }

        User foundUser = user.get();
        if (!PasswordEncoder.matches(password, foundUser.getPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(error);
        }

        if (!"admin".equals(foundUser.getRole())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "无权访问，仅管理员可登录");
            return ResponseEntity.status(403).body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "管理员登录成功");
        response.put("user", foundUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all-users")
    public ResponseEntity<?> getAllUsersForAdmin() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

}
