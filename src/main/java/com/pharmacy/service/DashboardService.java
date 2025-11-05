// DashboardService.java
package com.pharmacy.service;

import com.pharmacy.repository.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    @Autowired
    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 从数据库查询真实数据
        BigDecimal todaySales = dashboardRepository.getTodaySales();
        Integer todayOrders = dashboardRepository.getTodayOrders();
        Integer memberConsumption = dashboardRepository.getTodayMemberConsumption();
        Integer lowStockCount = dashboardRepository.getLowStockCount();

        stats.put("todaySales", todaySales != null ? todaySales : BigDecimal.ZERO);
        stats.put("todayOrders", todayOrders != null ? todayOrders : 0);
        stats.put("memberConsumption", memberConsumption != null ? memberConsumption : 0);
        stats.put("lowStockCount", lowStockCount != null ? lowStockCount : 0);

        // 同比变化（简化版，实际项目中应该从数据库计算）
        stats.put("salesChange", 12.5);
        stats.put("ordersChange", 8.2);
        stats.put("memberChange", 5.3);

        return stats;
    }

    public Map<String, Object> getSalesTrend(String period) {
        Map<String, Object> result = new HashMap<>();

        switch (period) {
            case "day":
                List<String> dayLabels = new ArrayList<>();
                dayLabels.add("00:00");
                dayLabels.add("03:00");
                dayLabels.add("06:00");
                dayLabels.add("09:00");
                dayLabels.add("12:00");
                dayLabels.add("15:00");
                dayLabels.add("18:00");
                dayLabels.add("21:00");
                result.put("labels", dayLabels);

                List<Integer> dayData = new ArrayList<>();
                dayData.add(320);
                dayData.add(150);
                dayData.add(80);
                dayData.add(650);
                dayData.add(1200);
                dayData.add(980);
                dayData.add(1500);
                dayData.add(850);
                result.put("data", dayData);
                break;
            case "week":
                List<String> weekLabels = new ArrayList<>();
                weekLabels.add("周一");
                weekLabels.add("周二");
                weekLabels.add("周三");
                weekLabels.add("周四");
                weekLabels.add("周五");
                weekLabels.add("周六");
                weekLabels.add("周日");
                result.put("labels", weekLabels);

                List<Integer> weekData = new ArrayList<>();
                weekData.add(1580);
                weekData.add(1820);
                weekData.add(1650);
                weekData.add(2100);
                weekData.add(2350);
                weekData.add(2080);
                weekData.add(1000);
                result.put("data", weekData);
                break;
            case "month":
                List<String> monthLabels = new ArrayList<>();
                monthLabels.add("第1周");
                monthLabels.add("第2周");
                monthLabels.add("第3周");
                monthLabels.add("第4周");
                result.put("labels", monthLabels);

                List<Integer> monthData = new ArrayList<>();
                monthData.add(15000);
                monthData.add(21000);
                monthData.add(18000);
                monthData.add(24000);
                result.put("data", monthData);
                break;
            default:
                List<String> defaultLabels = new ArrayList<>();
                defaultLabels.add("周一");
                defaultLabels.add("周二");
                defaultLabels.add("周三");
                defaultLabels.add("周四");
                defaultLabels.add("周五");
                defaultLabels.add("周六");
                defaultLabels.add("周日");
                result.put("labels", defaultLabels);

                List<Integer> defaultData = new ArrayList<>();
                defaultData.add(1580);
                defaultData.add(1820);
                defaultData.add(1650);
                defaultData.add(2100);
                defaultData.add(2350);
                defaultData.add(2080);
                defaultData.add(1000);
                result.put("data", defaultData);
        }

        return result;
    }

    public Map<String, Object> getCategoryDistribution() {
        Map<String, Object> result = new HashMap<>();

        List<String> labels = new ArrayList<>();
        labels.add("处方药");
        labels.add("非处方药");
        labels.add("保健品");
        labels.add("医疗器械");
        result.put("labels", labels);

        List<Integer> data = new ArrayList<>();
        data.add(35);
        data.add(45);
        data.add(15);
        data.add(5);
        result.put("data", data);

        List<String> colors = new ArrayList<>();
        colors.add("#165DFF");
        colors.add("#36B37E");
        colors.add("#FFAB00");
        colors.add("#FF5630");
        result.put("colors", colors);

        return result;
    }

    public List<Map<String, Object>> getStockAlerts() {
        return dashboardRepository.findStockAlerts();
    }

    public List<Map<String, Object>> getTodayHotProducts() {
        return dashboardRepository.findTodayHotProducts();
    }

    public List<Map<String, Object>> getExpiringMedicines() {
        return dashboardRepository.findExpiringMedicines();
    }

    // 添加导出数据方法
    public Map<String, Object> getExportData() {
        Map<String, Object> exportData = new HashMap<>();

        // 统计数据
        exportData.put("stats", getDashboardStats());

        // 销售趋势
        exportData.put("salesTrend", getSalesTrend("week"));

        // 分类占比
        exportData.put("categoryDistribution", getCategoryDistribution());

        // 库存预警
        exportData.put("stockAlerts", getStockAlerts());

        // 热销药品
        exportData.put("hotProducts", getTodayHotProducts());

        // 近效期药品
        exportData.put("expiringMedicines", getExpiringMedicines());

        // 导出时间
        exportData.put("exportTime", new java.util.Date());

        return exportData;
    }
}