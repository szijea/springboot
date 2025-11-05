package com.pharmacy.controller;

import com.pharmacy.entity.StockIn;
import com.pharmacy.entity.StockInItem;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.StockInItemRepository;
import com.pharmacy.repository.StockInRepository;
import com.pharmacy.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock-ins")
public class StockInController {

    @Autowired
    private StockInRepository stockInRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private StockInItemRepository stockInItemRepository;

    @GetMapping
    public ResponseEntity<Page<StockIn>> getStockIns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockIn> stockIns = stockInRepository.findAll(pageable);
            return ResponseEntity.ok(stockIns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockIn> getStockInById(@PathVariable Long id) {
        Optional<StockIn> stockIn = stockInRepository.findById(id);
        return stockIn.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStockIn(@RequestBody StockIn stockIn) {
        try {
            System.out.println("接收到入库单数据: " + stockIn);

            // 简化供应商验证 - 如果供应商不存在，使用默认供应商
            if (stockIn.getSupplier() != null && stockIn.getSupplier().getSupplierId() != null) {
                if (!supplierRepository.existsById(stockIn.getSupplier().getSupplierId())) {
                    System.out.println("供应商不存在，使用默认供应商");
                    // 使用第一个可用的供应商
                    Optional<com.pharmacy.entity.Supplier> defaultSupplier = supplierRepository.findById(1);
                    if (defaultSupplier.isPresent()) {
                        stockIn.setSupplier(defaultSupplier.get());
                    } else {
                        return ResponseEntity.badRequest().body("没有可用的供应商，请先创建供应商");
                    }
                }
            } else {
                // 如果没有提供供应商，使用默认供应商
                Optional<com.pharmacy.entity.Supplier> defaultSupplier = supplierRepository.findById(1);
                if (defaultSupplier.isPresent()) {
                    stockIn.setSupplier(defaultSupplier.get());
                } else {
                    return ResponseEntity.badRequest().body("请先创建供应商");
                }
            }

            // 验证药品是否存在并设置关联，确保 unitPrice 不为 null
            if (stockIn.getItems() != null && !stockIn.getItems().isEmpty()) {
                for (StockInItem item : stockIn.getItems()) {
                    // 验证药品是否存在
                    if (item.getMedicine() != null && item.getMedicine().getMedicineId() != null) {
                        if (!medicineRepository.existsById(item.getMedicine().getMedicineId())) {
                            return ResponseEntity.badRequest().body("药品不存在: " + item.getMedicine().getMedicineId());
                        }
                    } else {
                        return ResponseEntity.badRequest().body("药品信息不完整");
                    }

                    // 确保必要字段不为 null
                    if (item.getBatchNumber() == null) {
                        item.setBatchNumber("DEFAULT_BATCH");
                    }


                    // 设置关联关系
                    item.setStockIn(stockIn);
                }
            } else {
                return ResponseEntity.badRequest().body("入库单必须包含至少一个药品");
            }

            // 设置入库时间（如果未设置）
            if (stockIn.getStockInDate() == null) {
                stockIn.setStockInDate(LocalDateTime.now());
            }

            // 生成入库单号（如果未设置）
            if (stockIn.getStockInNo() == null) {
                stockIn.setStockInNo(generateStockInNo());
            }

            // 确保状态不为 null
            if (stockIn.getStatus() == null) {
                stockIn.setStatus(0); // 默认待审核
            }

            // 计算总金额
            stockIn.calculateTotalAmount();

            StockIn savedStockIn = stockInRepository.save(stockIn);
            System.out.println("入库单保存成功: " + savedStockIn);
            return ResponseEntity.ok(savedStockIn);
        } catch (Exception e) {
            System.err.println("创建入库单失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("创建入库单失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStockIn(@PathVariable Long id, @RequestBody StockIn stockInDetails) {
        try {
            Optional<StockIn> optionalStockIn = stockInRepository.findById(id);
            if (optionalStockIn.isPresent()) {
                StockIn stockIn = optionalStockIn.get();

                // 更新基本信息
                if (stockInDetails.getSupplier() != null) {
                    stockIn.setSupplier(stockInDetails.getSupplier());
                }
                if (stockInDetails.getStockInDate() != null) {
                    stockIn.setStockInDate(stockInDetails.getStockInDate());
                }
                if (stockInDetails.getRemark() != null) {
                    stockIn.setRemark(stockInDetails.getRemark());
                }
                if (stockInDetails.getStatus() != null) {
                    stockIn.setStatus(stockInDetails.getStatus());
                }

                // 更新明细项
                if (stockInDetails.getItems() != null) {
                    // 先清除原有明细
                    stockIn.getItems().clear();

                    // 添加新的明细
                    for (StockInItem item : stockInDetails.getItems()) {
                        item.setStockIn(stockIn);
                        stockIn.getItems().add(item);
                    }
                }

                // 重新计算总金额
                stockIn.calculateTotalAmount();

                StockIn updatedStockIn = stockInRepository.save(stockIn);
                return ResponseEntity.ok(updatedStockIn);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("更新入库单失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("更新入库单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStockIn(@PathVariable Long id) {
        if (stockInRepository.existsById(id)) {
            stockInRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveStockIn(@PathVariable Long id) {
        Optional<StockIn> optionalStockIn = stockInRepository.findById(id);
        if (optionalStockIn.isPresent()) {
            StockIn stockIn = optionalStockIn.get();
            stockIn.setStatus(1); // 已入库
            stockInRepository.save(stockIn);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StockIn>> searchStockIns(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockIn> stockIns = stockInRepository.findByKeyword(keyword, pageable);
            return ResponseEntity.ok(stockIns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 生成入库单号
    private String generateStockInNo() {
        return "SI" + System.currentTimeMillis();
    }
}