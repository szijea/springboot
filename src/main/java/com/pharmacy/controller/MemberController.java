package com.pharmacy.controller;

import com.pharmacy.dto.MemberStatsDTO;
import com.pharmacy.entity.Member;
import com.pharmacy.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList; // 添加这个导入
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberController {

    @Autowired
    private MemberService memberService;

    // 会员搜索端点
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            System.out.println("=== 会员搜索请求 ===");
            System.out.println("keyword: " + keyword);
            System.out.println("page: " + page);
            System.out.println("size: " + size);

            // 处理空关键词
            if (keyword == null || keyword.trim().isEmpty()) {
                System.out.println("关键词为空，返回提示信息");
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "请输入搜索关键词（姓名、手机号或卡号）");
                response.put("data", new ArrayList<>());
                response.put("total", 0);
                response.put("currentPage", page);
                response.put("totalPages", 0);
                return ResponseEntity.ok(response);
            }

            // 调用会员搜索服务
            List<Member> members = memberService.searchMembers(keyword.trim());

            // 简单的分页逻辑
            int total = members.size();
            int start = Math.min((page - 1) * size, total);
            int end = Math.min(start + size, total);
            List<Member> pagedMembers = (start < end) ? members.subList(start, end) : new ArrayList<>();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", pagedMembers);
            response.put("total", total);
            response.put("currentPage", page);
            response.put("totalPages", (int) Math.ceil((double) total / size));

            System.out.println("搜索完成，返回 " + pagedMembers.size() + " 个结果");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("会员搜索出错: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "会员搜索失败: " + e.getMessage());
            response.put("errorDetails", e.toString());
            response.put("data", new ArrayList<>());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 测试搜索端点
    @GetMapping("/test-search")
    public ResponseEntity<Map<String, Object>> testSearch(@RequestParam String keyword) {
        try {
            System.out.println("=== 测试会员搜索 ===");
            System.out.println("测试关键词: " + keyword);

            List<Member> members = memberService.searchMembers(keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "搜索测试成功");
            response.put("keyword", keyword);
            response.put("results", members);
            response.put("count", members.size());

            // 打印详细信息
            System.out.println("搜索到 " + members.size() + " 个会员:");
            members.forEach(member -> {
                System.out.println(" - " + member.getName() + " (手机: " + member.getPhone() + ")");
            });

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("测试搜索失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "测试搜索失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 健康检查端点
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Member Service");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // 测试端点
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Member Service is working! - " + java.time.LocalDateTime.now());
    }

    // 数据库测试端点
    @GetMapping("/test-db")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        try {
            // 测试数据库连接和基本操作
            long count = memberService.findAll().size();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "数据库连接正常");
            response.put("totalMembers", count);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("数据库测试失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "数据库连接失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 其他会员相关方法...
    @GetMapping
    public List<Member> getAllMembers() {
        return memberService.findAll();
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMemberById(@PathVariable String memberId) {
        Optional<Member> member = memberService.findById(memberId);
        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Member> getMemberByPhone(@PathVariable String phone) {
        Optional<Member> member = memberService.findByPhone(phone);
        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        try {
            // 如果前端没有提供memberId，自动生成
            if (member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
                member.setMemberId(memberService.generateNextMemberId());
            }

            Member savedMember = memberService.createMember(
                    member.getMemberId(),
                    member.getName(),
                    member.getPhone()
            );

            // 设置其他可选字段
            if (member.getCardNo() != null) {
                savedMember.setCardNo(member.getCardNo());
            }
            if (member.getAllergicHistory() != null) {
                savedMember.setAllergicHistory(member.getAllergicHistory());
            }
            if (member.getMedicalCardNo() != null) {
                savedMember.setMedicalCardNo(member.getMedicalCardNo());
            }

            return ResponseEntity.ok(savedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<?> updateMember(@PathVariable String memberId, @RequestBody Member member) {
        try {
            if (!memberId.equals(member.getMemberId())) {
                return ResponseEntity.badRequest().body("会员ID不匹配");
            }

            Member updatedMember = memberService.updateMember(member);
            return ResponseEntity.ok(updatedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> deleteMember(@PathVariable String memberId) {
        try {
            memberService.deleteMember(memberId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{memberId}/points/add")
    public ResponseEntity<?> addPoints(@PathVariable String memberId, @RequestParam int points) {
        boolean success = memberService.addPoints(memberId, points);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{memberId}/points/use")
    public ResponseEntity<?> usePoints(@PathVariable String memberId, @RequestParam int points) {
        boolean success = memberService.usePoints(memberId, points);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("积分不足或会员不存在");
        }
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhoneExists(@RequestParam String phone) {
        boolean exists = memberService.isPhoneExists(phone);
        return ResponseEntity.ok(exists);
    }


    // 在现有的 MemberController 中添加以下端点：

    // 获取会员统计数据
    @GetMapping("/stats")
    public ResponseEntity<MemberStatsDTO> getMemberStats() {
        try {
            MemberStatsDTO stats = memberService.getMemberStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("获取会员统计失败: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 批量删除会员
    @PostMapping("/batch-delete")
    public ResponseEntity<?> batchDeleteMembers(@RequestBody List<String> memberIds) {
        try {
            boolean success = memberService.deleteMembers(memberIds);
            if (success) {
                return ResponseEntity.ok().body("删除成功");
            } else {
                return ResponseEntity.badRequest().body("删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }

    // 筛选会员
    @GetMapping("/filter")
    public ResponseEntity<List<Member>> filterMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

            List<Member> members = memberService.filterMembers(name, phone, level, start, end);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取所有会员（分页版本）
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getMembersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<Member> allMembers = memberService.findAll();
            int total = allMembers.size();
            int start = Math.min(page * size, total);
            int end = Math.min(start + size, total);

            List<Member> pageMembers = allMembers.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("data", pageMembers);
            response.put("currentPage", page);
            response.put("totalItems", total);
            response.put("totalPages", (int) Math.ceil((double) total / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}