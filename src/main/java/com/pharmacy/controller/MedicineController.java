// MedicineController.java - 修复版本
package com.pharmacy.controller;

import com.pharmacy.dto.MedicineWithStockDTO;
import com.pharmacy.entity.Medicine;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = "*")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private InventoryService inventoryService;

    // 注意：这里只有药品相关的服务，没有memberService

    // 健康检查端点
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Medicine Service");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("port", 8080);
        return ResponseEntity.ok(response);
    }

    // 测试端点
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Medicine Service is working! - " + java.time.LocalDateTime.now());
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMedicines(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {

        System.out.println("=== 接收药品搜索请求 ===");
        System.out.println("keyword: " + keyword);
        System.out.println("category: " + category);
        System.out.println("page: " + page);
        System.out.println("size: " + size);

        try {
            Page<Medicine> medicinePage = medicineService.searchMedicines(keyword, category, page, size);
            List<Medicine> medicines = medicinePage.getContent();

            System.out.println("药品搜索结果数量: " + medicines.size());

            // 按 genericName+spec+manufacturer 分组并合并库存
            java.util.Map<String, java.util.List<Medicine>> grouped = medicines.stream()
                    .collect(java.util.stream.Collectors.groupingBy(m -> {
                        String g = m.getGenericName() != null ? m.getGenericName().trim().toLowerCase() : "";
                        String s = m.getSpec() != null ? m.getSpec().trim().toLowerCase() : "";
                        String mf = m.getManufacturer() != null ? m.getManufacturer().trim().toLowerCase() : "";
                        return g + "|" + s + "|" + mf;
                    }));

            java.util.List<java.util.Map<String, Object>> aggregated = new java.util.ArrayList<>();
            for (java.util.List<Medicine> groupList : grouped.values()) {
                Medicine rep = groupList.get(0);
                // 合并库存（将同组内所有 medicineId 的库存相加）
                int totalStock = 0;
                for (Medicine m : groupList) {
                    try {
                        Integer st = inventoryService.getCurrentStock(m.getMedicineId());
                        totalStock += (st != null ? st : 0);
                    } catch (Exception ex) {
                        System.err.println("聚合库存时出错: " + ex.getMessage());
                    }
                }

                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("medicineId", rep.getMedicineId());
                item.put("id", rep.getMedicineId());
                item.put("name", rep.getTradeName() != null ? rep.getTradeName() : rep.getGenericName());
                item.put("tradeName", rep.getTradeName());
                item.put("genericName", rep.getGenericName());
                item.put("spec", rep.getSpec());
                item.put("unit", rep.getUnit());
                item.put("approvalNo", rep.getApprovalNo());
                item.put("manufacturer", rep.getManufacturer());
                item.put("stockQuantity", totalStock);
                item.put("retailPrice", rep.getRetailPrice());
                item.put("categoryId", rep.getCategoryId());

                aggregated.add(item);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", aggregated);
            response.put("total", aggregated.size());
            response.put("currentPage", page);
            response.put("totalPages", 1);


            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("药品搜索出错: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "药品搜索失败: " + e.getMessage());
            response.put("data", List.of());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 其他药品相关的方法保持不变...
    @GetMapping
    public ResponseEntity<Page<Medicine>> getAllMedicines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Medicine> medicines = medicineService.getAllMedicines(pageable);
        return ResponseEntity.ok(medicines);
    }

    // 新增：创建药品（与前端 manualAddMedicine 对应）
    @PostMapping
    public ResponseEntity<?> createMedicine(@RequestBody Medicine medicine) {
        try {
            // 去重：优先按 approvalNo 去重
            if (medicine.getApprovalNo() != null && !medicine.getApprovalNo().trim().isEmpty()) {
                Medicine existing = medicineService.findByApprovalNo(medicine.getApprovalNo());
                if (existing != null) {
                    System.out.println("检测到已存在药品（approvalNo），复用现有记录: " + existing.getMedicineId());
                    return ResponseEntity.ok(existing);
                }
            } else {
                // 如果没有 approvalNo，则按 genericName+spec+manufacturer 去重
                Medicine existing = medicineService.findByGenericSpecManufacturer(medicine.getGenericName(), medicine.getSpec(), medicine.getManufacturer());
                if (existing != null) {
                    System.out.println("检测到已存在药品（generic+spec+manufacturer），复用现有记录: " + existing.getMedicineId());
                    return ResponseEntity.ok(existing);
                }
            }

            // 容错：填充必要默认字段，避免因为 null 或缺失字段导致保存失败
            if (medicine.getMedicineId() == null || medicine.getMedicineId().trim().isEmpty()) {
                medicine.setMedicineId("M" + System.currentTimeMillis());
            }
            if (medicine.getGenericName() == null || medicine.getGenericName().trim().isEmpty()) {
                medicine.setGenericName(medicine.getTradeName() != null ? medicine.getTradeName() : "手动添加药品");
            }
            if (medicine.getApprovalNo() == null || medicine.getApprovalNo().trim().isEmpty()) {
                medicine.setApprovalNo("手动添加-" + System.currentTimeMillis());
            }
            if (medicine.getCategoryId() == null) {
                medicine.setCategoryId(2);
            }
            if (medicine.getRetailPrice() == null) {
                medicine.setRetailPrice(new java.math.BigDecimal("0.00"));
            }

            try {
                Medicine created = medicineService.createMedicine(medicine);
                return ResponseEntity.ok(created);
            } catch (org.springframework.dao.DataIntegrityViolationException dive) {
                // 可能是唯一约束冲突（approvalNo），尝试按 approvalNo 查询并返回已存在记录
                System.err.println("唯一约束冲突，尝试按 approvalNo 查询已存在记录: " + dive.getMessage());
                if (medicine.getApprovalNo() != null) {
                    Medicine existing = medicineService.findByApprovalNo(medicine.getApprovalNo());
                    if (existing != null) {
                        return ResponseEntity.ok(existing);
                    }
                }
                throw dive;
            }
        } catch (Exception e) {
            System.err.println("创建药品失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "创建药品失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable String id) {
        Medicine medicine = medicineService.getMedicineById(id);
        if (medicine != null) {
            return ResponseEntity.ok(medicine);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search-with-stock")
    public ResponseEntity<Map<String, Object>> searchMedicinesWithStock(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {

        System.out.println("=== 接收药品搜索请求（包含库存） ===");
        System.out.println("keyword: " + keyword);
        System.out.println("category: " + category);
        System.out.println("page: " + page);
        System.out.println("size: " + size);

        try {
            Page<MedicineWithStockDTO> medicinePage = medicineService.searchMedicinesWithStock(keyword, category, page, size);
            List<MedicineWithStockDTO> medicines = medicinePage.getContent();

            System.out.println("药品搜索结果数量: " + medicines.size());

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", medicines);
            response.put("total", medicinePage.getTotalElements());
            response.put("currentPage", page);
            response.put("totalPages", medicinePage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("药品搜索出错: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "药品搜索失败: " + e.getMessage());
            response.put("data", List.of());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 删除所有与memberService相关的方法和引用
}