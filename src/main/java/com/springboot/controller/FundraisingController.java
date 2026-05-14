package com.springboot.controller;

import com.springboot.entity.DonationManage;
import com.springboot.entity.DonationRecord;
import com.springboot.entity.Fundraising;
import com.springboot.repository.DonationManageRepository;
import com.springboot.repository.DonationRecordRepository;
import com.springboot.repository.FundraisingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fundraising")
public class FundraisingController {

    @Autowired
    private FundraisingRepository fundraisingRepository;

    @Autowired
    private DonationRecordRepository donationRecordRepository;

    @Autowired
    private DonationManageRepository donationManageRepository;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Fundraising fundraising) {
        fundraising.setCurrentAmount(BigDecimal.ZERO);
        fundraising.setStatus("pending");
        fundraising.setCreateTime(LocalDateTime.now());
        fundraising.setUpdateTime(LocalDateTime.now());
        
        if (fundraising.getCreatorId() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "创建者信息缺失，请重新登录");
            return ResponseEntity.status(400).body(error);
        }
        
        fundraisingRepository.insert(fundraising);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "募捐项目已提交，等待审核");
        resp.put("data", fundraising);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        List<Fundraising> list = fundraisingRepository.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> listByStatus(@PathVariable String status) {
        List<Fundraising> list = fundraisingRepository.findByStatus(status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> detail(@PathVariable Integer id) {
        Optional<Fundraising> opt = fundraisingRepository.findById(id);
        if (!opt.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "项目不存在");
            return ResponseEntity.status(404).body(error);
        }
        Fundraising f = opt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("project", f);
        BigDecimal percent = BigDecimal.ZERO;
        if (f.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percent = f.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                    .divide(f.getTargetAmount(), 2, BigDecimal.ROUND_HALF_UP);
        }
        result.put("percentage", percent);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/donate")
    public ResponseEntity<?> donate(@RequestBody DonationRecord record) {
        Optional<Fundraising> opt = fundraisingRepository.findById(record.getFundraisingId());
        if (!opt.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "募捐项目不存在");
            return ResponseEntity.status(404).body(error);
        }
        Fundraising f = opt.get();
        if (!"ongoing".equals(f.getStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "当前项目不可捐赠");
            return ResponseEntity.status(400).body(error);
        }

        record.setPayTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        donationRecordRepository.insert(record);

        BigDecimal total = fundraisingRepository.sumDonatedAmount(record.getFundraisingId());
        fundraisingRepository.updateCurrentAmount(record.getFundraisingId(), total);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "捐赠成功");
        resp.put("record", record);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/my-donations/{userId}")
    public ResponseEntity<?> myDonations(@PathVariable Integer userId) {
        List<DonationRecord> list = donationRecordRepository.findByUserId(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/summary/{fundraisingId}")
    public ResponseEntity<?> summary(@PathVariable Integer fundraisingId) {
        Map<String, Object> summary = new HashMap<>();
        BigDecimal total = fundraisingRepository.sumDonatedAmount(fundraisingId);
        Long count = (long) donationRecordRepository.findByFundraisingId(fundraisingId).size();
        summary.put("totalAmount", total);
        summary.put("totalPeople", count);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/details/{fundraisingId}")
    public ResponseEntity<?> details(@PathVariable Integer fundraisingId) {
        List<DonationRecord> list = donationRecordRepository.findByFundraisingId(fundraisingId);
        for (DonationRecord dr : list) {
            if (dr.getIsAnonymous() != null && dr.getIsAnonymous()) {
                dr.setUserId(null);
            }
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/withdraw/apply")
    public ResponseEntity<?> applyWithdraw(@RequestBody DonationManage manage) {
        manage.setStatus(0);
        manage.setCreateTime(LocalDateTime.now());
        donationManageRepository.insert(manage);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "提现申请已提交，等待审核");
        resp.put("data", manage);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/withdraw/audit/{manageId}")
    public ResponseEntity<?> auditWithdraw(@PathVariable Integer manageId, @RequestParam Integer status) {
        Optional<DonationManage> opt = donationManageRepository.findById(manageId);
        if (!opt.isPresent() || opt.get().getStatus() != 0) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "审核失败，可能记录不存在或状态不正确");
            return ResponseEntity.badRequest().body(error);
        }
        donationManageRepository.updateStatus(manageId, status, LocalDateTime.now());

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "审核完成");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Integer id) {
        System.out.println("收到审核通过请求，ID: " + id);
        Optional<Fundraising> opt = fundraisingRepository.findById(id);
        if (!opt.isPresent() || !"pending".equals(opt.get().getStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "审核失败，项目不存在或状态不正确");
            return ResponseEntity.badRequest().body(error);
        }
        fundraisingRepository.updateStatus(id, "ongoing");

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "项目已发布");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable Integer id) {
        System.out.println("收到拒绝请求，ID: " + id);
        Optional<Fundraising> opt = fundraisingRepository.findById(id);
        if (!opt.isPresent() || !"pending".equals(opt.get().getStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "拒绝失败");
            return ResponseEntity.badRequest().body(error);
        }
        fundraisingRepository.updateStatus(id, "terminated");

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "项目已拒绝");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/terminate/{id}")
    public ResponseEntity<?> terminate(@PathVariable Integer id) {
        System.out.println("收到终止请求，ID: " + id);
        Optional<Fundraising> opt = fundraisingRepository.findById(id);
        if (!opt.isPresent() || !"ongoing".equals(opt.get().getStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "终止失败，可能不是进行中项目");
            return ResponseEntity.badRequest().body(error);
        }
        fundraisingRepository.updateStatus(id, "terminated");

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "项目已终止");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/complete/{id}")
    public ResponseEntity<?> complete(@PathVariable Integer id) {
        System.out.println("收到完成请求，ID: " + id);
        Optional<Fundraising> opt = fundraisingRepository.findById(id);
        if (!opt.isPresent() || !"ongoing".equals(opt.get().getStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "完成失败，可能不是进行中项目");
            return ResponseEntity.badRequest().body(error);
        }
        fundraisingRepository.updateStatus(id, "completed");

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "项目已完成");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/my-fundraisings/{userId}")
    public ResponseEntity<?> getMyFundraisings(@PathVariable Integer userId) {
        List<Fundraising> list = fundraisingRepository.findByCreatorId(userId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/my-complete/{id}")
    public ResponseEntity<?> myComplete(@PathVariable Integer id, @RequestParam Integer userId) {
        Optional<Fundraising> opt = fundraisingRepository.findById(id);
        if (!opt.isPresent() || !opt.get().getCreatorId().equals(userId) || !"ongoing".equals(opt.get().getStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "操作失败，请确认您是否有权限");
            return ResponseEntity.status(403).body(error);
        }
        fundraisingRepository.updateStatus(id, "completed");

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "项目已完成");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/my-terminate/{id}")
    public ResponseEntity<?> myTerminate(@PathVariable Integer id, @RequestParam Integer userId) {
        Optional<Fundraising> opt = fundraisingRepository.findById(id);
        if (!opt.isPresent() || !opt.get().getCreatorId().equals(userId) || !"ongoing".equals(opt.get().getStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "操作失败，请确认您是否有权限");
            return ResponseEntity.status(403).body(error);
        }
        fundraisingRepository.updateStatus(id, "terminated");

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "项目已终止");
        return ResponseEntity.ok(resp);
    }
}
