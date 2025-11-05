package com.pharmacy.service.impl;

import com.pharmacy.dto.OrderResponse;
import com.pharmacy.dto.OrderItemResponse;
import com.pharmacy.dto.OrderRequest;
import com.pharmacy.dto.OrderItemRequest;
import com.pharmacy.entity.Order;
import com.pharmacy.entity.OrderItem;
import com.pharmacy.entity.Medicine;
import com.pharmacy.repository.OrderRepository;
import com.pharmacy.repository.OrderItemRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.service.OrderService;
import com.pharmacy.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private InventoryService inventoryService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        System.out.println("=== 开始创建订单 ===");
        System.out.println("客户姓名: " + orderRequest.getCustomerName());
        System.out.println("会员ID: " + orderRequest.getMemberId());
        System.out.println("商品数量: " + orderRequest.getItems().size());

        try {
            // 1. 检查库存是否充足
            for (OrderItemRequest item : orderRequest.getItems()) {
                boolean stockAvailable = inventoryService.checkStock(item.getProductId(), item.getQuantity());
                if (!stockAvailable) {
                    String medicineName = medicineRepository.findById(item.getProductId())
                            .map(Medicine::getGenericName)
                            .orElse(item.getProductId());
                    throw new RuntimeException("药品 " + medicineName + " 库存不足，需求: " + item.getQuantity());
                }
            }

            // 2. 生成订单号
            String orderId = generateOrderId();
            System.out.println("生成的订单号: " + orderId);

            // 3. 计算订单总金额 - 使用前端传递的金额信息
            double totalAmount = orderRequest.getTotalAmount() != null ?
                    orderRequest.getTotalAmount().doubleValue() :
                    orderRequest.getItems().stream()
                            .mapToDouble(item -> item.getUnitPrice().doubleValue() * item.getQuantity())
                            .sum();

            // 使用前端传递的折扣信息
            double discountAmount = orderRequest.getDiscountAmount() != null ?
                    orderRequest.getDiscountAmount().doubleValue() : 0.0;
            double originalAmount = orderRequest.getOriginalAmount() != null ?
                    orderRequest.getOriginalAmount().doubleValue() : totalAmount;

            System.out.println("订单总金额: " + totalAmount);
            System.out.println("折扣金额: " + discountAmount);
            System.out.println("原始金额: " + originalAmount);

            // 4. 创建订单实体
            Order order = new Order();
            order.setOrderId(orderId);
            order.setCashierId(1); // 默认收银员ID

            // 设置会员ID
            if (orderRequest.getMemberId() != null && !orderRequest.getMemberId().trim().isEmpty()) {
                order.setMemberId(orderRequest.getMemberId().trim());
                System.out.println("设置会员ID: " + orderRequest.getMemberId());
            } else {
                order.setMemberId(null);
                System.out.println("无会员信息");
            }

            order.setCustomerName(orderRequest.getCustomerName());
            order.setTotalAmount(originalAmount);
            order.setDiscountAmount(discountAmount);
            order.setActualPayment(totalAmount);

            // 支付方式转换
            Integer paymentType = convertPaymentMethod(orderRequest.getPaymentMethod());
            order.setPaymentType(paymentType);
            order.setPaymentStatus(1); // 已支付
            order.setOrderTime(LocalDateTime.now());
            order.setPayTime(LocalDateTime.now());

            // 5. 保存订单
            Order savedOrder = orderRepository.save(order);
            System.out.println("订单保存成功，ID: " + savedOrder.getOrderId());
            System.out.println("订单会员ID: " + savedOrder.getMemberId());

            // 6. 创建订单项并更新库存
            for (OrderItemRequest itemRequest : orderRequest.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(savedOrder.getOrderId());
                orderItem.setMedicineId(itemRequest.getProductId());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setUnitPrice(itemRequest.getUnitPrice().doubleValue());
                orderItem.setSubtotal(itemRequest.getUnitPrice().doubleValue() * itemRequest.getQuantity());

                orderItemRepository.save(orderItem);
                System.out.println("订单项保存成功: " + itemRequest.getProductId() + " x " + itemRequest.getQuantity());

                // 更新库存
                boolean stockUpdated = inventoryService.updateStockForOrder(
                        itemRequest.getProductId(),
                        itemRequest.getQuantity(),
                        savedOrder.getOrderId()
                );

                if (!stockUpdated) {
                    String medicineName = medicineRepository.findById(itemRequest.getProductId())
                            .map(Medicine::getGenericName)
                            .orElse(itemRequest.getProductId());
                    throw new RuntimeException("库存更新失败: " + medicineName);
                }
            }

            // 7. 构建响应
            OrderResponse response = convertToOrderResponse(savedOrder);
            System.out.println("✅ 订单创建完成: " + response.getOrderNumber());

            return response;

        } catch (Exception e) {
            System.err.println("❌ 创建订单失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    // 支付方式转换方法
    private Integer convertPaymentMethod(String paymentMethod) {
        if (paymentMethod == null) return 1; // 默认现金

        switch (paymentMethod.toLowerCase()) {
            case "cash": return 1;
            case "wechat": return 2;
            case "alipay": return 3;
            case "insurance": return 4;
            default: return 1;
        }
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Optional<Order> getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // 这里需要根据你的状态枚举来设置
            return orderRepository.save(order);
        }
        throw new RuntimeException("订单不存在: " + orderId);
    }

    @Override
    @Transactional
    public boolean deleteOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        if (orderOpt.isPresent()) {
            // 先删除订单项
            orderItemRepository.deleteByOrderId(orderId);
            // 再删除订单
            orderRepository.deleteByOrderId(orderId);
            return true;
        }
        return false;
    }

    @Override
    public List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        return orderRepository.findByOrderTimeBetween(startDateTime, endDateTime);
    }

    @Override
    public List<Order> findOrdersByMemberId(String memberId) {
        return orderRepository.findByMemberId(memberId);
    }

    @Override
    public List<Order> getTodayOrders() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        return orderRepository.findByOrderTimeBetween(startOfDay, endOfDay);
    }

    @Override
    public Double getTotalSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        Double result = orderRepository.getTotalSalesByDateRange(startDateTime, endDateTime);
        return result != null ? result : 0.0;
    }

    @Override
    public Long getOrderCountByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        Long result = orderRepository.getOrderCountByDateRange(startDateTime, endDateTime);
        return result != null ? result : 0L;
    }

    // 新增：退单方法实现
    @Override
    @Transactional
    public Order refundOrder(String orderId, String reason) {
        System.out.println("=== 开始处理退单 ===");
        System.out.println("订单号: " + orderId);
        System.out.println("退单原因: " + reason);

        try {
            // 1. 查找订单
            Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
            if (!orderOpt.isPresent()) {
                throw new RuntimeException("订单不存在: " + orderId);
            }

            Order order = orderOpt.get();
            System.out.println("找到订单，当前状态: " + order.getPaymentStatus());
            System.out.println("订单金额: " + order.getActualPayment());

            // 2. 检查订单状态，只有已支付的订单才能退单
            if (order.getPaymentStatus() != 1) {
                throw new RuntimeException("只有已支付的订单才能退单，当前订单状态: " + getPaymentStatusText(order.getPaymentStatus()));
            }

            // 3. 更新订单状态为已退款
            order.setPaymentStatus(2); // 2表示已退款
            order.setRefundTime(LocalDateTime.now());
            order.setRefundReason(reason);

            // 4. 恢复库存
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            System.out.println("需要恢复库存的订单项数量: " + orderItems.size());

            for (OrderItem item : orderItems) {
                System.out.println("恢复库存 - 药品ID: " + item.getMedicineId() + ", 数量: " + item.getQuantity());

                // 获取药品信息用于日志
                String medicineName = medicineRepository.findById(item.getMedicineId())
                        .map(Medicine::getGenericName)
                        .orElse(item.getMedicineId());

                // 恢复库存
                boolean stockRestored = inventoryService.restoreStock(
                        item.getMedicineId(),
                        item.getQuantity(),
                        orderId + "_REFUND"
                );

                if (!stockRestored) {
                    System.err.println("警告: 药品 " + medicineName + " 库存恢复失败");
                    // 这里可以根据业务需求决定是否继续执行
                } else {
                    System.out.println("✅ 成功恢复药品库存: " + medicineName + " x " + item.getQuantity());
                }
            }

            // 5. 如果是会员订单，处理积分返还
            if (order.getMemberId() != null && !order.getMemberId().isEmpty()) {
                System.out.println("会员订单，需要处理积分返还:");
                System.out.println("  - 会员ID: " + order.getMemberId());
                System.out.println("  - 使用的积分: " + order.getUsedPoints());
                System.out.println("  - 获得的积分: " + order.getCreatedPoints());

                // 这里可以添加积分返还逻辑
                // 比如调用会员服务返还使用的积分，并扣除获得的积分
                // refundMemberPoints(order.getMemberId(), order.getUsedPoints(), order.getCreatedPoints());
            }

            // 6. 保存更新后的订单
            Order refundedOrder = orderRepository.save(order);
            System.out.println("✅ 退单处理完成: " + refundedOrder.getOrderId());
            System.out.println("退单后状态: " + refundedOrder.getPaymentStatus());
            System.out.println("退款时间: " + refundedOrder.getRefundTime());

            return refundedOrder;

        } catch (Exception e) {
            System.err.println("❌ 退单处理失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("退单失败: " + e.getMessage());
        }
    }

    // 辅助方法：获取支付状态文本
    private String getPaymentStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已退款";
            default: return "未知状态(" + status + ")";
        }
    }

    // === 私有辅助方法 ===

    private String generateOrderId() {
        // 生成格式如: O20241027123456 的订单号
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "O" + timestamp;
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderNumber(order.getOrderId());
        response.setCustomerName(order.getCustomerName());
        response.setTotalAmount(java.math.BigDecimal.valueOf(order.getTotalAmount()));
        response.setStatus("已完成");
        response.setCreateTime(order.getOrderTime());

        // 获取订单项
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }

    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductId(orderItem.getMedicineId());

        // 获取药品信息
        Optional<Medicine> medicineOpt = medicineRepository.findById(orderItem.getMedicineId());
        if (medicineOpt.isPresent()) {
            Medicine medicine = medicineOpt.get();
            response.setProductName(medicine.getGenericName());
        } else {
            response.setProductName("未知药品");
        }

        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(java.math.BigDecimal.valueOf(orderItem.getUnitPrice()));
        response.setSubtotal(java.math.BigDecimal.valueOf(orderItem.getSubtotal()));

        return response;
    }
}