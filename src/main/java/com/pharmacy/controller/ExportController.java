// ExportController.java
package com.pharmacy.controller;

import com.pharmacy.dto.ApiResponse;
import com.pharmacy.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final DashboardService dashboardService;

    @Autowired
    public ExportController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PostMapping("/dashboard-report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportDashboardReport() {
        try {
            Map<String, Object> exportData = dashboardService.getExportData();
            return ResponseEntity.ok(ApiResponse.success("报表导出成功", exportData));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("报表导出失败"));
        }
    }
}