package com.springboot.service;

import com.springboot.entity.DonationManage;
import com.springboot.entity.DonationRecord;
import com.springboot.entity.Fundraising;
import com.springboot.repository.DonationManageRepository;
import com.springboot.repository.DonationRecordRepository;
import com.springboot.repository.FundraisingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FundraisingService {

    @Autowired
    private FundraisingRepository fundraisingRepository;

    @Autowired
    private DonationRecordRepository donationRecordRepository;

    @Autowired
    private DonationManageRepository donationManageRepository;

    // 创建募捐项目（状态为待审核）
    @Transactional
    public Fundraising createFundraising(Fundraising fundraising) {
        fundraising.setCurrentAmount(BigDecimal.ZERO);
        fundraising.setStatus("pending");   // 待审核
        fundraising.setCreateTime(LocalDateTime.now());
        fundraising.setUpdateTime(LocalDateTime.now());
        fundraisingRepository.insert(fundraising);
        return fundraising;
    }
    // 在 FundraisingService.java 中添加
    public List<DonationRecord> getUserDonations(Integer userId) {
        return donationRecordRepository.findByUserId(userId);
    }
    // 审核通过 -> 发布
    @Transactional
    public boolean approveFundraising(Integer fundraisingId) {
        Optional<Fundraising> opt = fundraisingRepository.findById(fundraisingId);
        if (!opt.isPresent()) return false;
        Fundraising f = opt.get();
        if (!"pending".equals(f.getStatus())) return false;
        fundraisingRepository.updateStatus(fundraisingId, "ongoing");
        return true;
    }

    // 审核拒绝 -> 终止
    @Transactional
    public boolean rejectFundraising(Integer fundraisingId) {
        Optional<Fundraising> opt = fundraisingRepository.findById(fundraisingId);
        if (!opt.isPresent()) return false;
        Fundraising f = opt.get();
        if (!"pending".equals(f.getStatus())) return false;
        fundraisingRepository.updateStatus(fundraisingId, "terminated");
        return true;
    }

    // 手动终止项目
    @Transactional
    public boolean terminateFundraising(Integer fundraisingId) {
        Optional<Fundraising> opt = fundraisingRepository.findById(fundraisingId);
        if (!opt.isPresent()) return false;
        Fundraising f = opt.get();
        if (!"ongoing".equals(f.getStatus())) return false;
        fundraisingRepository.updateStatus(fundraisingId, "terminated");
        return true;
    }

    // 完成项目（到期自动或手动）
    @Transactional
    public boolean completeFundraising(Integer fundraisingId) {
        Optional<Fundraising> opt = fundraisingRepository.findById(fundraisingId);
        if (!opt.isPresent()) return false;
        Fundraising f = opt.get();
        if (!"ongoing".equals(f.getStatus())) return false;
        fundraisingRepository.updateStatus(fundraisingId, "completed");
        return true;
    }

    // 用户捐赠
    @Transactional
    public DonationRecord donate(DonationRecord record) {
        record.setPayTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        donationRecordRepository.insert(record);

        // 更新项目已筹金额
        BigDecimal total = fundraisingRepository.sumDonatedAmount(record.getFundraisingId());
        fundraisingRepository.updateCurrentAmount(record.getFundraisingId(), total);
        return record;
    }

    // 获取项目详情（含进度）
    public Map<String, Object> getProjectDetail(Integer fundraisingId) {
        Optional<Fundraising> opt = fundraisingRepository.findById(fundraisingId);
        if (!opt.isPresent()) return null;
        Fundraising f = opt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("project", f);
        BigDecimal percent = BigDecimal.ZERO;
        if (f.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percent = f.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                    .divide(f.getTargetAmount(), 2, BigDecimal.ROUND_HALF_UP);
        }
        result.put("percentage", percent);
        return result;
    }

    // 申请提现（创建资金管理记录）
    @Transactional
    public DonationManage applyWithdraw(DonationManage manage) {
        manage.setStatus(0); // 待审核
        manage.setCreateTime(LocalDateTime.now());
        donationManageRepository.insert(manage);
        return manage;
    }

    // 审核提现申请
    @Transactional
    public boolean auditWithdraw(Integer manageId, Integer status) {
        Optional<DonationManage> opt = donationManageRepository.findById(manageId);
        if (!opt.isPresent()) return false;
        DonationManage dm = opt.get();
        if (dm.getStatus() != 0) return false; // 只能审核待审核的
        donationManageRepository.updateStatus(manageId, status, LocalDateTime.now());
        return true;
    }

    // 资金公示（汇总公示）
    public Map<String, Object> getSummaryByProject(Integer fundraisingId) {
        Map<String, Object> summary = new HashMap<>();
        BigDecimal total = fundraisingRepository.sumDonatedAmount(fundraisingId);
        Long count = (long) donationRecordRepository.findByFundraisingId(fundraisingId).size();
        summary.put("totalAmount", total);
        summary.put("totalPeople", count);
        return summary;
    }

    // 资金公示（明细公示，脱敏）
    public List<DonationRecord> getDetailRecords(Integer fundraisingId) {
        List<DonationRecord> list = donationRecordRepository.findByFundraisingId(fundraisingId);
        // 脱敏：匿名用户隐藏真实姓名
        for (DonationRecord dr : list) {
            if (dr.getIsAnonymous() != null && dr.getIsAnonymous()) {
                dr.setUserId(null);   // 或者只显示匿名标识
            }
        }
        return list;
    }

    public List<Fundraising> getAllFundraising() {
        return fundraisingRepository.findAll();
    }

    public List<Fundraising> getFundraisingByStatus(String status) {
        return fundraisingRepository.findByStatus(status);
    }
}