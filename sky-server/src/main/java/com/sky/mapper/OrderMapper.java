package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersHistoryQueryDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper {
    /**
     * 新增订单信息
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据id查询订单信息
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> historyPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 统计数量
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer count(Integer status);

    /**
     * 批量取消超时未支付订单
     * @param pendingPaymentStatus 待支付状态
     * @param timeoutTime 超时时间点
     * @param cancelledStatus 取消状态
     * @param cancelReason 取消原因
     * @param cancelTime 取消时间
     * @return 影响行数
     */
    Integer cancelTimeoutOrders(@Param("pendingPaymentStatus") Integer pendingPaymentStatus,
                                @Param("timeoutTime") LocalDateTime timeoutTime,
                                @Param("cancelledStatus") Integer cancelledStatus,
                                @Param("cancelReason") String cancelReason,
                                @Param("cancelTime") LocalDateTime cancelTime);

    /**
     * 凌晨自动完成配送中的订单
     * @param deliveryInProgressStatus 配送中状态
     * @param completedStatus 已完成状态
     * @param deliveryTime 送达时间
     * @return 影响行数
     */
    Integer completeDeliveryInProgressOrders(@Param("deliveryInProgressStatus") Integer deliveryInProgressStatus,
                                             @Param("completedStatus") Integer completedStatus,
                                             @Param("deliveryTime") LocalDateTime deliveryTime);
}
