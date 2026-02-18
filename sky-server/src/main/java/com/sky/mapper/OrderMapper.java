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
import java.util.Map;
import java.util.List;

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

    /**
     * 按天统计营业额
     * @param beginTime 开始时间（含）
     * @param endTime 结束时间（不含）
     * @param status 订单状态
     * @return 日期和营业额列表
     */
    List<Map<String, Object>> getTurnoverStatistics(@Param("beginTime") LocalDateTime beginTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("status") Integer status);

    /**
     * 按天统计订单总数和有效订单数
     * @param beginTime 开始时间（含）
     * @param endTime 结束时间（不含）
     * @param validStatus 有效订单状态
     * @return 日期和订单统计列表
     */
    List<Map<String, Object>> getOrderStatistics(@Param("beginTime") LocalDateTime beginTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("validStatus") Integer validStatus);

    /**
     * 根据动态条件统计订单数量
     * @param map 查询条件
     * @return 订单数量
     */
    Integer countByMap(Map<String, Object> map);

    /**
     * 根据动态条件汇总营业额
     * @param map 查询条件
     * @return 营业额
     */
    Double sumByMap(Map<String, Object> map);
}
