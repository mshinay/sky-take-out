package com.sky.service;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersHistoryQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {
    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    public OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 查询订单详细
     * @param orderId
     * @return
     */
    OrdersDTO getOrdersInfo(Long orderId);

    /**
     * 查询历史订单
     * @param ordersHistoryQueryDTO
     * @return
     */
    PageResult getHistoryOrdersInfo(OrdersHistoryQueryDTO ordersHistoryQueryDTO);
}
