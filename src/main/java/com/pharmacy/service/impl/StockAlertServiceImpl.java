package com.pharmacy.service.impl;

import com.pharmacy.entity.Inventory;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.StockAlert;
import com.pharmacy.repository.InventoryRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.StockAlertRepository;
import com.pharmacy.service.StockAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockAlertServiceImpl implements StockAlertService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private StockAlertRepository stockAlertRepository;

    private static final int EXPIRY_WARNING_DAYS = 60;

    @Override
    public List<StockAlert> getAllAlerts() {
        return stockAlertRepository.findAll();
    }

    @Override
    public List<StockAlert> getUnhandledAlerts() {
        return stockAlertRepository.findByIsHandledFalse();
    }

    @Override
    public List<StockAlert> getAlertsByType(Integer alertType) {
        return stockAlertRepository.findByAlertTypeAndIsHandledFalse(alertType);
    }

    @Override
    public boolean handleAlert(Long alertId) {
        return stockAlertRepository.findById(alertId)
                .map(alert -> {
                    alert.setIsHandled(true);
                    stockAlertRepository.save(alert);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Map<String, Object> getDashboardStockAlerts() {
        Map<String, Object> result = new HashMap<>();

        result.put("expiry", getExpiringMedicines());
        result.put("lowStock", getLowStockMedicines());
        result.put("outStock", getOutOfStockMedicines());

        return result;
    }

    @Override
    public void checkAndGenerateAlerts() {
        // 这里实现自动检查库存并生成预警的逻辑
        // 由于时间关系，暂时简单实现

        try {
            // 检查低库存
            List<Inventory> lowStockInventories = inventoryRepository.findLowStock();
            for (Inventory inventory : lowStockInventories) {
                // 检查是否已经存在未处理的相同预警
                List<StockAlert> existingAlerts = stockAlertRepository.findByMedicineIdAndAlertTypeAndIsHandledFalse(
                        inventory.getMedicineId(), 1); // 1-库存不足

                if (existingAlerts.isEmpty()) {
                    StockAlert alert = new StockAlert();
                    alert.setMedicineId(inventory.getMedicineId());
                    alert.setAlertType(1); // 库存不足
                    alert.setCurrentStock(inventory.getStockQuantity());
                    alert.setMinStock(inventory.getMinStock());
                    alert.setAlertMessage("药品库存低于安全库存");
                    alert.setIsHandled(false);
                    stockAlertRepository.save(alert);
                }
            }

            // 检查近效期药品
            LocalDate warningDate = LocalDate.now().plusDays(EXPIRY_WARNING_DAYS);
            List<Inventory> expiringInventories = inventoryRepository.findExpiringSoon(LocalDate.now(), warningDate);

            for (Inventory inventory : expiringInventories) {
                if (inventory.getExpiryDate() != null) {
                    List<StockAlert> existingAlerts = stockAlertRepository.findByMedicineIdAndAlertTypeAndIsHandledFalse(
                            inventory.getMedicineId(), 2); // 2-近效期

                    if (existingAlerts.isEmpty()) {
                        StockAlert alert = new StockAlert();
                        alert.setMedicineId(inventory.getMedicineId());
                        alert.setAlertType(2); // 近效期
                        alert.setCurrentStock(inventory.getStockQuantity());
                        alert.setExpiryDate(inventory.getExpiryDate().atStartOfDay());
                        alert.setAlertMessage("药品即将过期");
                        alert.setIsHandled(false);
                        stockAlertRepository.save(alert);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("生成库存预警失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getExpiringMedicines() {
        List<Map<String, Object>> expiringMedicines = new ArrayList<>();

        try {
            // 获取近效期药品
            LocalDate warningDate = LocalDate.now().plusDays(EXPIRY_WARNING_DAYS);
            List<Inventory> inventories = inventoryRepository.findExpiringSoon(LocalDate.now(), warningDate);

            for (Inventory inventory : inventories) {
                Map<String, Object> medicine = new HashMap<>();
                Medicine med = inventory.getMedicine();

                medicine.put("id", inventory.getId().toString());
                medicine.put("medicineId", inventory.getMedicineId());
                medicine.put("medicineName", med != null ? med.getGenericName() : "未知药品");
                medicine.put("name", med != null ? med.getGenericName() : "未知药品");
                medicine.put("specification", med != null ? med.getSpec() : "未知规格");
                medicine.put("spec", med != null ? med.getSpec() : "未知规格");
                medicine.put("batchNumber", inventory.getBatchNo());
                medicine.put("batch", inventory.getBatchNo());
                medicine.put("expiryDate", inventory.getExpiryDate().toString());
                medicine.put("currentStock", inventory.getStockQuantity());
                medicine.put("remainingStock", inventory.getStockQuantity());
                medicine.put("remaining", inventory.getStockQuantity());
                medicine.put("unit", med != null ? med.getUnit() : "盒");

                // 计算距离过期的天数
                long daysToExpiry = LocalDate.now().until(inventory.getExpiryDate()).getDays();
                medicine.put("daysToExpiry", daysToExpiry);
                medicine.put("days", daysToExpiry);

                // 根据天数设置优先级
                if (daysToExpiry <= 15) {
                    medicine.put("priority", "high");
                } else if (daysToExpiry <= 30) {
                    medicine.put("priority", "medium");
                } else {
                    medicine.put("priority", "low");
                }

                expiringMedicines.add(medicine);
            }

        } catch (Exception e) {
            System.err.println("获取近效期药品失败: " + e.getMessage());
            // 返回空列表或模拟数据
        }

        return expiringMedicines;
    }

    @Override
    public List<Map<String, Object>> getLowStockMedicines() {
        List<Map<String, Object>> lowStockMedicines = new ArrayList<>();

        try {
            List<Inventory> inventories = inventoryRepository.findLowStock();

            for (Inventory inventory : inventories) {
                Map<String, Object> medicine = new HashMap<>();
                Medicine med = inventory.getMedicine();

                medicine.put("id", inventory.getId().toString());
                medicine.put("medicineId", inventory.getMedicineId());
                medicine.put("medicineName", med != null ? med.getGenericName() : "未知药品");
                medicine.put("name", med != null ? med.getGenericName() : "未知药品");
                medicine.put("specification", med != null ? med.getSpec() : "未知规格");
                medicine.put("spec", med != null ? med.getSpec() : "未知规格");
                medicine.put("batchNumber", inventory.getBatchNo());
                medicine.put("batch", inventory.getBatchNo());
                medicine.put("currentStock", inventory.getStockQuantity());
                medicine.put("remainingStock", inventory.getStockQuantity());
                medicine.put("remaining", inventory.getStockQuantity());
                medicine.put("safetyStock", inventory.getMinStock());
                medicine.put("minStock", inventory.getMinStock());
                medicine.put("unit", med != null ? med.getUnit() : "盒");

                // 计算库存比例
                double stockRatio = inventory.getMinStock() > 0 ?
                        (double) inventory.getStockQuantity() / inventory.getMinStock() : 0;

                if (stockRatio <= 0.1) {
                    medicine.put("priority", "high");
                } else {
                    medicine.put("priority", "medium");
                }

                lowStockMedicines.add(medicine);
            }

        } catch (Exception e) {
            System.err.println("获取低库存药品失败: " + e.getMessage());
        }

        return lowStockMedicines;
    }

    @Override
    public List<Map<String, Object>> getOutOfStockMedicines() {
        List<Map<String, Object>> outOfStockMedicines = new ArrayList<>();

        try {
            // 查找库存为0的药品
            List<Inventory> inventories = inventoryRepository.findAll();
            for (Inventory inventory : inventories) {
                if (inventory.getStockQuantity() == 0) {
                    Map<String, Object> medicine = new HashMap<>();
                    Medicine med = inventory.getMedicine();

                    medicine.put("id", inventory.getId().toString());
                    medicine.put("medicineId", inventory.getMedicineId());
                    medicine.put("medicineName", med != null ? med.getGenericName() : "未知药品");
                    medicine.put("name", med != null ? med.getGenericName() : "未知药品");
                    medicine.put("specification", med != null ? med.getSpec() : "未知规格");
                    medicine.put("spec", med != null ? med.getSpec() : "未知规格");
                    medicine.put("batchNumber", inventory.getBatchNo());
                    medicine.put("batch", inventory.getBatchNo());
                    medicine.put("currentStock", 0);
                    medicine.put("safetyStock", inventory.getMinStock());
                    medicine.put("minStock", inventory.getMinStock());
                    medicine.put("unit", med != null ? med.getUnit() : "盒");
                    medicine.put("lastRestock", inventory.getUpdateTime() != null ?
                            inventory.getUpdateTime().toLocalDate().toString() : "未知日期");
                    medicine.put("priority", "high");

                    outOfStockMedicines.add(medicine);
                }
            }

        } catch (Exception e) {
            System.err.println("获取缺货药品失败: " + e.getMessage());
        }

        return outOfStockMedicines;
    }
}