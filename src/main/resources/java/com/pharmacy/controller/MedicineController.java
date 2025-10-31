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

    // 其他药品相关的方法保持不变...
    @GetMapping
    public ResponseEntity<Page<Medicine>> getAllMedicines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Medicine> medicines = medicineService.getAllMedicines(pageable);
        return ResponseEntity.ok(medicines);
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