package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_in_item")
public class StockInItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "stock_in_id")
    private StockIn stockIn;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "unit_price")
    private Double unitPrice = 0.0;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "production_date")
    private LocalDateTime productionDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "remark", length = 200)
    private String remark;

    // 构造器
    public StockInItem() {}

    public StockInItem(Medicine medicine, Integer quantity, Double unitPrice) {
        this.medicine = medicine;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getter和Setter
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public StockIn getStockIn() { return stockIn; }
    public void setStockIn(StockIn stockIn) { this.stockIn = stockIn; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public LocalDateTime getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDateTime productionDate) { this.productionDate = productionDate; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    // 计算小计金额
    public Double getSubtotal() {
        if (quantity == null || unitPrice == null) {
            return 0.0;
        }
        return quantity * unitPrice;
    }
}