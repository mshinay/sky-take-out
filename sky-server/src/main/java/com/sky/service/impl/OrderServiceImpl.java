package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersHistoryQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常
        //查看地址是否为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new RuntimeException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查看购物车是否为空
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> list = shoppingCartMapper.select(shoppingCart);
        if(list == null || list.size() == 0){
            throw new RuntimeException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //插入订单表
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));

        orderMapper.insert(orders);
        //插入订单详细表
        List<OrderDetail> detailList = new ArrayList<OrderDetail>();
        for(ShoppingCart shoppingCart1 : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart1, orderDetail);
            orderDetail.setOrderId(orders.getId());
            detailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(detailList);
        //清空购物车
        shoppingCartMapper.deleteAllByUserId(BaseContext.getCurrentId());
        //封装OrderSubmitVO并返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );*/
        //生成空的的预支付交易单
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public OrdersDTO getOrdersInfo(Long orderId) {
        //获取订单信息
        Orders orders = orderMapper.getById(orderId);
        //获取订单详细信息
        List<OrderDetail> detailList = orderDetailMapper.getByOrdersId(orderId);
        OrdersDTO ordersDTO = new OrdersDTO();
        BeanUtils.copyProperties(orders, ordersDTO);
        ordersDTO.setOrderDetails(detailList);
        return ordersDTO;
    }

    @Override
    public PageResult getHistoryOrdersInfo(OrdersHistoryQueryDTO ordersHistoryQueryDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(ordersHistoryQueryDTO.getPage(), ordersHistoryQueryDTO.getPageSize());

        //Page<>是由pagehelper封装的返回集合

        Page<Orders> pages = orderMapper.historyPageQuery(ordersHistoryQueryDTO);
        List<Orders> ordersList =pages.getResult();
        List<OrderVO> orderVOList = new ArrayList<>();
        for(Orders orders : ordersList){
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrdersId(orders.getId());
            OrderVO orderVO = new OrderVO();
            //把orders的数据复制给orderVO
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetailList);
            orderVOList.add(orderVO);
        }
        return new PageResult(pages.getTotal(), orderVOList);
    }
}
