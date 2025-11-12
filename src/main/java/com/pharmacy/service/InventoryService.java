package com.pharmacy.service;

import com.pharmacy.entity.Inventory;
import com.pharmacy.dto.InventoryDTO;
import java.util.List;

public interface InventoryService {

    /**
     * 新增：返回带药品信息的 InventoryDTO 列表，避免懒加载问题
     * @return 带药品信息的 InventoryDTO 列表
     */
    List<InventoryDTO> findAllWithMedicineDTO();

    /**
     * 为订单更新库存
     * @param medicineId 药品ID
     * @param quantity 数量
     * @param orderId 订单ID
     * @return 是否更新成功
     */
    boolean updateStockForOrder(String medicineId, Integer quantity, String orderId);

    /**
     * 恢复库存（用于退单）
     * @param medicineId 药品ID
     * @param quantity 数量
     * @param refundOrderId 退单ID
     * @return 是否恢复成功
     */
    boolean restoreStock(String medicineId, Integer quantity, String refundOrderId);

    /**
     * 检查库存是否充足
     * @param medicineId 药品ID
     * @param quantity 需求数量
     * @return 是否充足
     */
    boolean checkStock(String medicineId, Integer quantity);

    /**
     * 获取当前库存
     * @param medicineId 药品ID
     * @return 库存数量
     */
    Integer getCurrentStock(String medicineId);

    /**
     * 获取即将过期的药品
     * @return 即将过期药品列表
     */
    List<Inventory> getExpiringSoon();

    /**
     * 获取所有库存信息
     * @return 所有库存信息
     */
    List<Inventory> findAll();

    /**
     * 获取低库存药品
     * @return 低库存药品列表
     */
    List<Inventory> getLowStock();

    /**
     * 根据ID获取库存信息
     * @param id 库存ID
     * @return 库存信息
     */
    Inventory findById(Long id);

    /**
     * 保存库存信息
     * @param inventory 库存信息
     * @return 保存后的库存信息
     */
    Inventory save(Inventory inventory);

    /**
     * 删除库存信息
     * @param id 库存ID
     */
    void deleteById(Long id);
}