// Setting.java
package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_phone")
    private String storePhone;

    @Column(name = "store_address")
    private String storeAddress;

    @Column(name = "store_desc")
    private String storeDesc;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 10;

    @Column(name = "notify_methods")
    private String notifyMethods;

    @Column(name = "points_rule")
    private Double pointsRule = 1.0;

    @Column(name = "cash_rule")
    private Integer cashRule = 100;

    @Column(name = "operation_log")
    private Boolean operationLog = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStorePhone() {
        return storePhone;
    }

    public void setStorePhone(String storePhone) {
        this.storePhone = storePhone;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStoreDesc() {
        return storeDesc;
    }

    public void setStoreDesc(String storeDesc) {
        this.storeDesc = storeDesc;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getNotifyMethods() {
        return notifyMethods;
    }

    public void setNotifyMethods(String notifyMethods) {
        this.notifyMethods = notifyMethods;
    }

    public Double getPointsRule() {
        return pointsRule;
    }

    public void setPointsRule(Double pointsRule) {
        this.pointsRule = pointsRule;
    }

    public Integer getCashRule() {
        return cashRule;
    }

    public void setCashRule(Integer cashRule) {
        this.cashRule = cashRule;
    }

    public Boolean getOperationLog() {
        return operationLog;
    }

    public void setOperationLog(Boolean operationLog) {
        this.operationLog = operationLog;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}