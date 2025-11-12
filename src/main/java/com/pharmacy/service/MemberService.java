package com.pharmacy.service;

import com.pharmacy.dto.MemberStatsDTO;
import com.pharmacy.entity.Member;
import com.pharmacy.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList; // 添加这个导入
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    // 改进的搜索方法 - 同时搜索所有条件并去重
    public List<Member> searchMembers(String keyword) {
        try {
            System.out.println("=== 在MemberService中搜索会员 ===");
            System.out.println("搜索关键词: " + keyword);

            if (keyword == null || keyword.trim().isEmpty()) {
                System.out.println("关键词为空，返回空列表");
                return new ArrayList<>();
            }

            String trimmedKeyword = keyword.trim();
            Set<Member> results = new HashSet<>();

            // 1. 按手机号精确匹配
            System.out.println("正在按手机号搜索: " + trimmedKeyword);
            Optional<Member> byPhone = memberRepository.findByPhone(trimmedKeyword);
            if (byPhone.isPresent()) {
                System.out.println("按手机号找到会员: " + byPhone.get().getName());
                results.add(byPhone.get());
            } else {
                System.out.println("按手机号未找到会员");
            }

            // 2. 按姓名模糊搜索
            System.out.println("正在按姓名模糊搜索: " + trimmedKeyword);
            List<Member> byName = memberRepository.findByNameContaining(trimmedKeyword);
            if (!byName.isEmpty()) {
                System.out.println("按姓名找到 " + byName.size() + " 个会员");
                results.addAll(byName);
            } else {
                System.out.println("按姓名未找到会员");
            }

            // 3. 按会员卡号搜索
            System.out.println("正在按会员卡号搜索: " + trimmedKeyword);
            Optional<Member> byCardNo = memberRepository.findByCardNo(trimmedKeyword);
            if (byCardNo.isPresent()) {
                System.out.println("按卡号找到会员: " + byCardNo.get().getName());
                results.add(byCardNo.get());
            } else {
                System.out.println("按卡号未找到会员");
            }

            System.out.println("总共找到 " + results.size() + " 个不重复的会员");
            return new ArrayList<>(results);
        } catch (Exception e) {
            System.err.println("会员搜索异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("搜索会员时发生错误: " + e.getMessage(), e);
        }
    }

    // 创建新会员
    public Member createMember(String memberId, String name, String phone) {
        // 检查手机号是否已存在
        if (memberRepository.findByPhone(phone).isPresent()) {
            throw new RuntimeException("手机号已存在: " + phone);
        }

        Member member = new Member(memberId, name, phone);
        member.setCreateTime(LocalDateTime.now());
        return memberRepository.save(member);
    }

    // 根据ID查找会员
    public Optional<Member> findById(String memberId) {
        return memberRepository.findById(memberId);
    }

    // 根据手机号查找会员
    public Optional<Member> findByPhone(String phone) {
        return memberRepository.findByPhone(phone);
    }

    // 获取所有会员
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // 更新会员信息
    public Member updateMember(Member member) {
        if (!memberRepository.existsById(member.getMemberId())) {
            throw new RuntimeException("会员不存在: " + member.getMemberId());
        }
        return memberRepository.save(member);
    }

    // 删除会员
    public void deleteMember(String memberId) {
        memberRepository.deleteById(memberId);
    }

    // 增加积分
    public boolean addPoints(String memberId, int points) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.addPoints(points);
            memberRepository.save(member);
            return true;
        }
        return false;
    }

    // 使用积分
    public boolean usePoints(String memberId, int points) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            boolean success = member.usePoints(points);
            if (success) {
                memberRepository.save(member);
            }
            return success;
        }
        return false;
    }

    // 检查手机号是否存在
    public boolean isPhoneExists(String phone) {
        return memberRepository.findByPhone(phone).isPresent();
    }

    // 生成下一个会员ID（简单实现）
    public String generateNextMemberId() {
        // 这里可以根据业务规则实现更复杂的ID生成逻辑
        List<Member> members = memberRepository.findAll();
        if (members.isEmpty()) {
            return "M00001";
        }

        // 获取最大的会员ID并递增
        String maxId = members.stream()
                .map(Member::getMemberId)
                .max(String::compareTo)
                .orElse("M00000");

        // 提取数字部分并递增
        int number = Integer.parseInt(maxId.substring(1)) + 1;
        return String.format("M%05d", number);
    }

    // 在现有的 MemberService 类中添加以下方法：

    // 获取会员统计数据
    public MemberStatsDTO getMemberStats() {
        MemberStatsDTO stats = new MemberStatsDTO();

        try {
            // 总会员数
            Long total = memberRepository.countTotalMembers();
            stats.setTotalMembers(total != null ? total : 0L);

            // VIP会员数（假设等级>=3为VIP）
            List<Member> vipMembers = memberRepository.findByLevel(3);
            stats.setVipMembers((long) vipMembers.size());

            // 最近30天新增会员
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Long newMembers = memberRepository.countNewMembersSince(thirtyDaysAgo);
            stats.setNewMembers(newMembers != null ? newMembers : 0L);

            // 设置模拟数据（实际项目中应从订单服务获取）
            stats.setTodayConsumption(89L);
            stats.setTotalConsumption(328560.0);
            stats.setSleepingMembers(128L);

            // 设置增长率（模拟数据）
            stats.setTotalGrowth(32);
            stats.setVipGrowth(12);
            stats.setNewGrowth(15);
            stats.setTodayGrowth(12);
            stats.setConsumptionGrowth(15.2);
            stats.setSleepingGrowth(-18);

        } catch (Exception e) {
            System.err.println("获取会员统计数据失败: " + e.getMessage());
            // 设置默认值
            stats.setTotalMembers(0L);
            stats.setVipMembers(0L);
            stats.setNewMembers(0L);
            stats.setTodayConsumption(0L);
            stats.setTotalConsumption(0.0);
            stats.setSleepingMembers(0L);
        }

        return stats;
    }

    // 批量删除会员
    @Transactional
    public boolean deleteMembers(List<String> memberIds) {
        try {
            for (String memberId : memberIds) {
                memberRepository.deleteById(memberId);
            }
            return true;
        } catch (Exception e) {
            System.err.println("批量删除会员失败: " + e.getMessage());
            return false;
        }
    }

    // 根据多个条件筛选会员
    public List<Member> filterMembers(String name, String phone, Integer level, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // 这里可以实现更复杂的查询逻辑
            // 目前简化实现：如果提供了具体条件就按条件查询，否则返回所有
            if (name != null && !name.trim().isEmpty()) {
                return memberRepository.findByNameContaining(name.trim());
            } else if (phone != null && !phone.trim().isEmpty()) {
                Optional<Member> member = memberRepository.findByPhone(phone.trim());
                return member.map(List::of).orElse(new ArrayList<>());
            } else if (level != null) {
                return memberRepository.findByLevel(level);
            } else {
                return memberRepository.findAll();
            }
        } catch (Exception e) {
            System.err.println("筛选会员失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}