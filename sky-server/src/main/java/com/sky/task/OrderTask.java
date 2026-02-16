package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 每分钟检查一次超时未支付订单并自动取消
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cancelTimeoutOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutTime = now.minusMinutes(15);
        Integer affectedRows = orderMapper.cancelTimeoutOrders(
                Orders.PENDING_PAYMENT,
                timeoutTime,
                Orders.CANCELLED,
                MessageConstant.ORDER_TIMEOUT,
                now
        );
        log.info("定时取消超时订单结束，阈值时间：{}，影响行数：{}", timeoutTime, affectedRows);
    }

    /**
     * 每天凌晨2点自动完成配送中的订单
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void completeDeliveryInProgressOrders() {
        LocalDateTime now = LocalDateTime.now();
        Integer affectedRows = orderMapper.completeDeliveryInProgressOrders(
                Orders.DELIVERY_IN_PROGRESS,
                Orders.COMPLETED,
                now
        );
        log.info("定时完成配送中订单结束，完成时间：{}，影响行数：{}", now, affectedRows);
    }
}
