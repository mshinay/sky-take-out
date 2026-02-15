package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult> orderList(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("管理端进行订单查询{}",ordersPageQueryDTO);
        PageResult pageResult = orderService.getPageOrdersInfo(ordersPageQueryDTO);
        //log.info("管理端进行订单查询{}",pageResult.getRecords().get(1));
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> orderStatistics() {
        log.info("订单数据统计");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详细
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        log.info("管理端查询详细订单{}",id);
        OrderVO orderVO = orderService.getOrdersInfo(id);
        return Result.success(orderVO);
    }

    /**
     * 接受订单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("管理端接受订单{}",ordersConfirmDTO);
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception{
        log.info("管理端取消订单{}",ordersCancelDTO);
        orderService.cancelByManage(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 管理端拒绝订单
     * @param ordersRejectionDTO
     * @return
     * @throws Exception
     */
    @PutMapping("/rejection")
    public Result RejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception{
        log.info("管理端拒绝订单{}",ordersRejectionDTO);
        orderService.reject(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 管理端派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    public Result deliveryOrder(@PathVariable Long id) {
        log.info("管理端派送订单{}",id);
        orderService.deliverOrder(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result completeOrder(@PathVariable Long id) {
        log.info("管理端完成订单{}",id);
        orderService.completeOrder(id);
        return Result.success();
    }
}

