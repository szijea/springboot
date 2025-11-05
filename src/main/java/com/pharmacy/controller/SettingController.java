// SettingController.java
package com.pharmacy.controller;

import com.pharmacy.entity.Setting;
import com.pharmacy.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingController {

    @Autowired
    private SettingRepository settingRepository;

    @GetMapping
    public ResponseEntity<?> getSettings() {
        try {
            Setting settings = settingRepository.findLatestSettings();
            if (settings == null) {
                // 如果没有设置，返回默认值
                settings = new Setting();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("storeName", settings.getStoreName());
            response.put("storePhone", settings.getStorePhone());
            response.put("storeAddress", settings.getStoreAddress());
            response.put("storeDesc", settings.getStoreDesc());
            response.put("lowStockThreshold", settings.getLowStockThreshold());
            response.put("pointsRule", settings.getPointsRule());
            response.put("cashRule", settings.getCashRule());
            response.put("operationLog", settings.getOperationLog());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("获取设置失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> settingsData) {
        try {
            Setting settings = settingRepository.findLatestSettings();
            if (settings == null) {
                settings = new Setting();
                settings.setCreatedAt(LocalDateTime.now());
            }

            // 更新基本设置
            if (settingsData.containsKey("storeName")) {
                settings.setStoreName((String) settingsData.get("storeName"));
            }
            if (settingsData.containsKey("storePhone")) {
                settings.setStorePhone((String) settingsData.get("storePhone"));
            }
            if (settingsData.containsKey("storeAddress")) {
                settings.setStoreAddress((String) settingsData.get("storeAddress"));
            }
            if (settingsData.containsKey("storeDesc")) {
                settings.setStoreDesc((String) settingsData.get("storeDesc"));
            }

            // 更新库存设置
            if (settingsData.containsKey("lowStockThreshold")) {
                settings.setLowStockThreshold(Integer.valueOf(settingsData.get("lowStockThreshold").toString()));
            }

            // 更新会员设置
            if (settingsData.containsKey("pointsRule")) {
                settings.setPointsRule(Double.valueOf(settingsData.get("pointsRule").toString()));
            }
            if (settingsData.containsKey("cashRule")) {
                settings.setCashRule(Integer.valueOf(settingsData.get("cashRule").toString()));
            }

            // 更新操作日志设置
            if (settingsData.containsKey("operationLog")) {
                settings.setOperationLog(Boolean.valueOf(settingsData.get("operationLog").toString()));
            }

            settings.setUpdatedAt(LocalDateTime.now());
            settingRepository.save(settings);

            return ResponseEntity.ok("设置保存成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("保存设置失败: " + e.getMessage());
        }
    }
}