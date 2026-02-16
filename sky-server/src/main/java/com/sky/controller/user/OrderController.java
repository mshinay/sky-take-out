package com.sky.controller.user;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
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
    public Result<OrderVO> getOrdersInfo(@PathVariable Long id){
        log.info("查询订单详细{}", id);
        OrderVO orderVO = orderService.getOrdersInfo(id);
        return Result.success(orderVO);
    }



    /**
     * 查询历史订单详细信息
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> getHistoryOrdersInfo(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("查询订单详细{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.getPageOrdersInfo(ordersPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) throws Exception {
        log.info("取消订单{}", id);
        orderService.cancelOrder(id);
        return Result.success();
    }



    /**
     * 再来一单
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    public Result repeatOrder(@PathVariable Long id){
        log.info("复制订单{}", id);
        orderService.repeatOrder(id);
        return Result.success();
    }

    /**
     * 催单
     * @param id
     * @return
     */
    @RequestMapping(value = "/reminder/{id}", method = {RequestMethod.GET, RequestMethod.PUT})
    public Result reminder(@PathVariable Long id){
        log.info("催单{}", id);
        orderService.reminder(id);
        return Result.success();
    }
}
