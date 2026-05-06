package com.springboot.controller;

import com.springboot.entity.User;
import com.springboot.entity.UserActivity;
import com.springboot.entity.VolunteerActivity;
import com.springboot.repository.UserActivityRepository;
import com.springboot.repository.UserRepository;
import com.springboot.repository.VolunteerActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/volunteer")
@CrossOrigin(origins = "http://localhost:5173")
public class VolunteerActivityController {

    @Autowired
    private VolunteerActivityRepository activityRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private UserRepository userRepository;   // 注入用户 repository


    // ==================== 志愿者端接口（保持不变） ====================

    /**
     * 1. 活动列表（可选状态筛选：recruiting / ongoing / ended）
     */
    @GetMapping("/activities")
    public ResponseEntity<?> getActivities(@RequestParam(required = false) String status) {
        List<VolunteerActivity> activities;
        if ("recruiting".equals(status)) {
            activities = activityRepository.findByRecruitStatus("recruiting");
        } else {
            activities = activityRepository.findAll();
        }
        return ResponseEntity.ok(activities);
    }

    /**
     * 2. 活动详情
     */
    @GetMapping("/activities/{id}")
    public ResponseEntity<?> getActivityDetail(@PathVariable Integer id) {
        Optional<VolunteerActivity> activity = activityRepository.findById(id);
        return activity.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body((VolunteerActivity) Map.of("message", "活动不存在")));
    }

    /**
     * 3. 报名活动（自动审核通过）
     */
    @PostMapping("/activities/{id}/register")
    public ResponseEntity<?> registerActivity(@PathVariable Integer id,
                                              @RequestParam Integer userId) {
        // 1. 检查用户是否为志愿者
        Optional<User> optUser = userRepository.findById(userId.longValue());
        if (!optUser.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }
        User user = optUser.get();
        if (!Boolean.TRUE.equals(user.getIsVolunteer())) {
            return ResponseEntity.status(403).body(Map.of("message", "只有志愿者才能报名活动，请先申请成为志愿者"));
        }

        // 2. 检查活动是否存在
        Optional<VolunteerActivity> optActivity = activityRepository.findById(id);
        if (!optActivity.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "活动不存在"));
        }
        VolunteerActivity activity = optActivity.get();
        if (!"recruiting".equals(activity.getRecruitStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "活动已停止招募"));
        }

        // 3. 检查是否已经报名
        Optional<UserActivity> existing = userActivityRepository.findByUserAndActivity(userId, id);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "您已经报名过此活动"));
        }

        // 4. 报名 + 自动审核
        userActivityRepository.register(userId, id);
        return ResponseEntity.ok(Map.of("message", "报名成功，先等待管理员审核,审核通过后可在报名当天签到"));
    }

    /**
     * 4. 取消报名
     */
    @DeleteMapping("/activities/{id}/register")
    public ResponseEntity<?> cancelRegistration(@PathVariable Integer id,
                                                @RequestParam Integer userId) {
        int rows = userActivityRepository.cancelRegistration(userId, id);
        if (rows > 0) {
            return ResponseEntity.ok(Map.of("message", "取消报名成功"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "无法取消报名，可能已签到或活动已结束"));
        }
    }

    /**
     * 5. 签到
     */
    @PostMapping("/activities/{id}/checkin")
    public ResponseEntity<?> checkin(@PathVariable Integer id,
                                     @RequestParam Integer userId) {
        Optional<VolunteerActivity> optActivity = activityRepository.findById(id);
        if (!optActivity.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "活动不存在"));
        }
        VolunteerActivity activity = optActivity.get();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            return ResponseEntity.badRequest().body(Map.of("message", "不在活动时间内，无法签到"));
        }
        int rows = userActivityRepository.checkin(userId, id);
        if (rows > 0) {
            return ResponseEntity.ok(Map.of("message", "签到成功"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "签到失败，请确认报名是否已通过审核"));
        }
    }
    /**
     * 管理员审核报名
     */
    // 获取所有待审核的报名记录（含活动名称、申请人姓名等）
    @GetMapping("/admin/pending-registrations")
    public ResponseEntity<?> getPendingRegistrations() {
        return ResponseEntity.ok(userActivityRepository.findPendingRegistrations());
    }

    // 审核通过报名
    @PutMapping("/admin/registrations/{activityId}/approve")
    public ResponseEntity<?> approveRegistration(@PathVariable Integer activityId,
                                                 @RequestParam Integer userId) {
        int rows = userActivityRepository.approveRegistration(userId, activityId);
        if (rows > 0) {
            return ResponseEntity.ok(Map.of("message", "报名审核通过"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "审核失败（可能记录不存在或状态不是 pending）"));
        }
    }

    // 拒绝报名（删除记录）
    @DeleteMapping("/admin/registrations/{activityId}/reject")
    public ResponseEntity<?> rejectRegistration(@PathVariable Integer activityId,
                                                @RequestParam Integer userId) {
        int rows = userActivityRepository.cancelRegistration(userId, activityId);
        if (rows > 0) {
            return ResponseEntity.ok(Map.of("message", "已拒绝该报名"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "拒绝失败"));
        }
    }
    /**
     * 6. 管理员：计算活动时长（活动结束后调用）
     */
    @PostMapping("/activities/{id}/calculate-duration")
    public ResponseEntity<?> calculateDuration(@PathVariable Integer id) {
        Optional<VolunteerActivity> optActivity = activityRepository.findById(id);
        if (!optActivity.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "活动不存在"));
        }
        VolunteerActivity activity = optActivity.get();
        if (LocalDateTime.now().isBefore(activity.getEndTime())) {
            return ResponseEntity.badRequest().body(Map.of("message", "活动尚未结束，不能计算时长"));
        }
        int rows = userActivityRepository.calculateDurationForActivity(id);
        return ResponseEntity.ok(Map.of("message", "已为 " + rows + " 位志愿者计算服务时长"));
    }

    // ==================== 管理员端接口（活动管理） ====================

    /**
     * 7. 创建活动（管理员）
     */
    @PostMapping("/admin/activities")
    public ResponseEntity<?> createActivity(@RequestBody VolunteerActivity activity,
                                            @RequestParam Integer adminId) {
        // 可选：校验 adminId 是否为管理员（建议在 Service 层或通过 Security 完成，此处仅做基本非空判断）
        if (activity.getActivityName() == null || activity.getActivityName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "活动名称不能为空"));
        }
        activity.setRecruitStatus("recruiting");   // 新活动默认识别中
        activity.setCreatorId(adminId);
        activityRepository.insert(activity);
        return ResponseEntity.ok(Map.of("message", "活动创建成功", "activity", activity));
    }

    /**
     * 8. 更新活动（管理员）
     */
    @PutMapping("/admin/activities/{id}")
    public ResponseEntity<?> updateActivity(@PathVariable Integer id,
                                            @RequestBody VolunteerActivity activity) {
        Optional<VolunteerActivity> existing = activityRepository.findById(id);
        if (!existing.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "活动不存在"));
        }
        activity.setActivityId(id);
        // 保留原有的 creatorId 不变
        activity.setCreatorId(existing.get().getCreatorId());
        activityRepository.update(activity);
        return ResponseEntity.ok(Map.of("message", "活动更新成功"));
    }

    /**
     * 9. 删除活动（管理员）- 会级联删除报名记录（由数据库外键约束或手动处理）
     */
    @DeleteMapping("/admin/activities/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Integer id) {
        Optional<VolunteerActivity> existing = activityRepository.findById(id);
        if (!existing.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "活动不存在"));
        }
        activityRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "活动已删除"));
    }

    /**
     * 10. 修改招募状态（管理员）
     */
    @PutMapping("/admin/activities/{id}/recruit-status")
    public ResponseEntity<?> updateRecruitStatus(@PathVariable Integer id,
                                                 @RequestParam String status) {
        if (!"recruiting".equals(status) && !"closed".equals(status)) {
            return ResponseEntity.badRequest().body(Map.of("message", "状态值错误，仅支持 recruiting 或 closed"));
        }
        Optional<VolunteerActivity> existing = activityRepository.findById(id);
        if (!existing.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "活动不存在"));
        }
        activityRepository.updateRecruitStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "招募状态已更新为 " + ("recruiting".equals(status) ? "招募中" : "已关闭")));
    }
}