package com.springboot.controller;

import com.springboot.entity.DonationManage;
import com.springboot.entity.DonationRecord;
import com.springboot.entity.Fundraising;
import com.springboot.service.FundraisingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fundraising")
@CrossOrigin(origins = "http://localhost:5173")
public class FundraisingController {

    @Autowired
    private FundraisingService fundraisingService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Fundraising fundraising) {
        fundraising.setStatus("pending");
        Fundraising created = fundraisingService.createFundraising(fundraising);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "募捐项目已提交，等待审核");
        resp.put("data", created);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        List<Fundraising> list = fundraisingService.getAllFundraising();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> listByStatus(@PathVariable String status) {
        List<Fundraising> list = fundraisingService.getFundraisingByStatus(status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> detail(@PathVariable Integer id) {
        Map<String, Object> detail = fundraisingService.getProjectDetail(id);
        if (detail == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "项目不存在");
            return ResponseEntity.status(404).body(error);
        }
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/donate")
    public ResponseEntity<?> donate(@RequestBody DonationRecord record) {
        Map<String, Object> project = fundraisingService.getProjectDetail(record.getFundraisingId());
        if (project == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "募捐项目不存在");
            return ResponseEntity.status(404).body(error);
        }
        Fundraising f = (Fundraising) project.get("project");
        if (!"ongoing".equals(f.getStatus())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "当前项目不可捐赠");
            return ResponseEntity.status(400).body(error);
        }
        DonationRecord saved = fundraisingService.donate(record);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "捐赠成功");
        resp.put("record", saved);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/my-donations/{userId}")
    public ResponseEntity<?> myDonations(@PathVariable Integer userId) {
        List<DonationRecord> list = fundraisingService.getUserDonations(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/summary/{fundraisingId}")
    public ResponseEntity<?> summary(@PathVariable Integer fundraisingId) {
        Map<String, Object> summary = fundraisingService.getSummaryByProject(fundraisingId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/details/{fundraisingId}")
    public ResponseEntity<?> details(@PathVariable Integer fundraisingId) {
        List<DonationRecord> list = fundraisingService.getDetailRecords(fundraisingId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/withdraw/apply")
    public ResponseEntity<?> applyWithdraw(@RequestBody DonationManage manage) {
        DonationManage saved = fundraisingService.applyWithdraw(manage);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "提现申请已提交，等待审核");
        resp.put("data", saved);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/withdraw/audit/{manageId}")
    public ResponseEntity<?> auditWithdraw(@PathVariable Integer manageId, @RequestParam Integer status) {
        boolean ok = fundraisingService.auditWithdraw(manageId, status);
        if (!ok) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "审核失败，可能记录不存在或状态不正确");
            return ResponseEntity.badRequest().body(error);
        }
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "审核完成");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Integer id) {
        boolean ok = fundraisingService.approveFundraising(id);
        if (!ok) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "审核失败，项目不存在或状态不正确");
            return ResponseEntity.badRequest().body(error);
        }
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "项目已发布");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable Integer id) {
        boolean ok = fundraisingService.rejectFundraising(id);
        if (!ok) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "拒绝失败");
            return ResponseEntity.badRequest().body(error);
        }
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "项目已拒绝");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/terminate/{id}")
    public ResponseEntity<?> terminate(@PathVariable Integer id) {
        boolean ok = fundraisingService.terminateFundraising(id);
        if (!ok) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "终止失败，可能不是进行中项目");
            return ResponseEntity.badRequest().body(error);
        }
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "项目已终止");
        return ResponseEntity.ok(resp);
    }
}
