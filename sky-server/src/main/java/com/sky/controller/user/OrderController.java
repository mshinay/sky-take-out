package com.sky.controller.user;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersHistoryQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> orderSubmit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交订单:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.orderSubmit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);

        //模拟交易成功
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        log.info("模拟交易成功{}", ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询订单详细信息
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrdersDTO> getOrdersInfo(@PathVariable Long id){
        log.info("查询订单详细{}", id);
        OrdersDTO ordersDTO = orderService.getOrdersInfo(id);
        return Result.success(ordersDTO);
    }

    /**
     * 查询历史订单详细信息
     * @param ordersHistoryQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> getHistoryOrdersInfo(OrdersHistoryQueryDTO ordersHistoryQueryDTO){
        log.info("查询订单详细{}", ordersHistoryQueryDTO);
        PageResult pageResult = orderService.getHistoryOrdersInfo(ordersHistoryQueryDTO);
        return Result.success(pageResult);
    }
}
