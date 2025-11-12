package com.pharmacy.repository;

import com.pharmacy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    List<Order> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByMemberId(String memberId);

    void deleteByOrderId(String orderId);

    // 支付订单的销售额统计
    @Query("SELECT COALESCE(SUM(o.actualPayment), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end AND o.paymentStatus = 1")
    Double getTotalSalesByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 支付订单的数量统计
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderTime BETWEEN :start AND :end AND o.paymentStatus = 1")
    Long getOrderCountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 所有订单的总销售额（不考虑支付状态）
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end")
    Double getTotalSalesByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 所有订单的平均金额
    @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end")
    Double getAverageOrderValueByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 时间段内的订单数量
    Long countByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

    // 每日销售额统计
    @Query("SELECT DATE(o.orderTime), COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end GROUP BY DATE(o.orderTime) ORDER BY DATE(o.orderTime)")
    List<Object[]> getDailySales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 按支付状态统计订单数量
    @Query("SELECT CAST(o.paymentStatus AS string), COUNT(o) FROM Order o GROUP BY o.paymentStatus")
    List<Object[]> countOrdersByStatus();

    // 获取客户消费统计
    @Query("SELECT o.customerName, COUNT(o), COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderTime BETWEEN :start AND :end GROUP BY o.customerName ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> getCustomerSpendingStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 在 OrderRepository.java 中添加这些方法

    // 获取今日销售额（已支付订单）
    @Query("SELECT COALESCE(SUM(o.actualPayment), 0) FROM Order o WHERE DATE(o.orderTime) = CURRENT_DATE AND o.paymentStatus = 1")
    Double getTodaySales();

    // 获取今日订单数（已支付订单）
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.orderTime) = CURRENT_DATE AND o.paymentStatus = 1")
    Long getTodayOrderCount();

    // 获取昨日销售额（已支付订单）
    @Query("SELECT COALESCE(SUM(o.actualPayment), 0) FROM Order o WHERE DATE(o.orderTime) = :yesterday AND o.paymentStatus = 1")
    Double getYesterdaySales(@Param("yesterday") LocalDate yesterday);

    // 获取昨日订单数（已支付订单）
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.orderTime) = :yesterday AND o.paymentStatus = 1")
    Long getYesterdayOrderCount(@Param("yesterday") LocalDate yesterday);

    // 获取今日会员消费人数
    @Query("SELECT COUNT(DISTINCT o.memberId) FROM Order o WHERE DATE(o.orderTime) = CURRENT_DATE AND o.paymentStatus = 1 AND o.memberId IS NOT NULL")
    Integer getTodayMemberConsumption();

    // 获取昨日会员消费人数
    @Query("SELECT COUNT(DISTINCT o.memberId) FROM Order o WHERE DATE(o.orderTime) = :yesterday AND o.paymentStatus = 1 AND o.memberId IS NOT NULL")
    Integer getYesterdayMemberConsumption(@Param("yesterday") LocalDate yesterday);

    // 获取今日热销药品
    @Query(value = "SELECT m.medicine_id, m.generic_name, m.trade_name, m.spec, m.retail_price, " +
            "SUM(oi.quantity) as total_sold, SUM(oi.quantity * oi.unit_price) as total_amount " +
            "FROM order_item oi " +
            "JOIN medicine m ON oi.medicine_id = m.medicine_id " +
            "JOIN `order` o ON oi.order_id = o.order_id " +
            "WHERE DATE(o.order_time) = CURRENT_DATE AND o.payment_status = 1 " +
            "GROUP BY m.medicine_id, m.generic_name, m.trade_name, m.spec, m.retail_price " +
            "ORDER BY total_sold DESC LIMIT 10", nativeQuery = true)
    List<Object[]> getTodayHotProducts();
}
