package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.properties.BaiduProperties;
import com.sky.properties.ShopProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private static final String BAIDU_GEOCODING_API = "https://api.map.baidu.com/geocoding/v3/";
    private static final String BAIDU_DIRECTION_API = "https://api.map.baidu.com/directionlite/v1/riding";
    private static final int MAX_DELIVERY_DISTANCE_METERS = 5000;

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
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private ShopProperties shopProperties;
    @Autowired
    private BaiduProperties baiduProperties;

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

        // 校验收货地址是否超出配送范围（5km），地图服务异常时降级放行
        checkDeliveryRange(addressBook);

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
        String address = addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setAddress(address);
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

    /**
     * 获取订单详细信息
     * @param orderId
     * @return
     */
    @Override
    public OrderVO getOrdersInfo(Long orderId) {
        //获取订单信息
        Orders orders = orderMapper.getById(orderId);
        //获取订单详细信息
        List<OrderDetail> detailList = orderDetailMapper.getByOrdersId(orderId);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(detailList);
        return orderVO;
    }



    /**
     * 查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult getHistoryOrdersInfo(OrdersPageQueryDTO ordersPageQueryDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        //Page<>是由pagehelper封装的返回集合

        Page<Orders> pages = orderMapper.historyPageQuery(ordersPageQueryDTO);
        List<Orders> ordersList =pages.getResult();
        List<OrderVO> orderVOList = new ArrayList<>();
        if(pages != null && pages.getTotal() > 0) {
            for (Orders orders : ordersList) {
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrdersId(orders.getId());
                OrderVO orderVO = new OrderVO();
                //把orders的数据复制给orderVO
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(pages.getTotal(), orderVOList);
    }



    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancelOrder(Long id) throws Exception{
        //得到原始订单信息
        Orders ordersDB = orderMapper.getById(id);
        //防止为空
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        //如果处在待接单状态，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        //修改订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }



    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repeatOrder(Long id) {
        //根据id查询订单
        Orders orders = orderMapper.getById(id);
        //防止为空
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //将菜品加回购物车
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrdersId(id);
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for(OrderDetail orderDetail : orderDetailList){
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart,"id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }

        shoppingCartMapper.insertBatch(shoppingCartList);
        
    }

    @Override
    public OrderStatisticsVO statistics() {
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        Integer toBeConfirmed = orderMapper.count(2);
        Integer deliveryInProgress = orderMapper.count(4);
        Integer confirmed = orderMapper.count(5);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        orderStatisticsVO.setConfirmed(confirmed);
        return orderStatisticsVO;

    }

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult getPageOrdersInfo(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = this.getHistoryOrdersInfo(ordersPageQueryDTO);
        //得到视图数据
        List<OrderVO> orderVOList = pageResult.getRecords();
        //对每一列在客户端上呈现的数据进行处理
        for (OrderVO orderVO : orderVOList) {
            orderVO.setOrderDishes(this.ordersDetailList(orderVO.getOrderDetailList()));

        }
        //log.info("管理端试图{}",orderVOList);
        pageResult.setRecords(orderVOList);
        return pageResult;
    }

    /**
     * 管理端接受订单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        //从数据库中得到订单信息
        Orders orderDB = orderMapper.getById(ordersConfirmDTO.getId());
        //非空判定
        if(orderDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //判断是否付费
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消

        if (orderDB.getStatus() > Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改订单信息
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderDB, orders);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);

    }

    /**
     * 管理端取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelByManage(OrdersCancelDTO ordersCancelDTO) throws Exception {
        //得到原始订单信息
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
        //防止为空
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() < 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());


        //如果处在待接单状态，需要进行退款
        if (ordersDB.getPayStatus().equals(Orders.PAID)){
            /*weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额*/

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        //修改订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 管理端拒绝订单
     * @param ordersRejectionDTO
     */
    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) throws Exception{
        //获得订单信息
        Orders orderDB = orderMapper.getById(ordersRejectionDTO.getId());
        //非空检验
        if(orderDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //状态判断
        if (orderDB.getStatus() != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        log.info("orderDB {}", orderDB);
        if (orderDB.getPayStatus().equals(Orders.PAID)){
           /* weChatPayUtil.refund(
                    orderDB.getNumber(), //商户订单号
                    orderDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额*/


        }
        //状态修改
        Orders orders = new Orders();
        //支付状态修改为 退款
        orders.setPayStatus(Orders.REFUND);
        BeanUtils.copyProperties(orderDB, orders);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 管理端派送订单
     * @param id
     */
    @Override
    public void deliverOrder(Long id) {
        //获取订单信息
        Orders orderDB = orderMapper.getById(id);
        //非空判断
        if(orderDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orderDB.getStatus() != Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改状态
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderDB, orders);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public void completeOrder(Long id) {
        //获取订单信息
        Orders orderDB = orderMapper.getById(id);
        //非空判断
        if(orderDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orderDB.getStatus() != Orders.DELIVERY_IN_PROGRESS){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改状态
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderDB, orders);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    private String ordersDetailList(List<OrderDetail> orderDetailList) {
        StringBuffer sb = new StringBuffer();
        for (OrderDetail orderDetail : orderDetailList) {
            sb.append(orderDetail.getName()+"*"+orderDetail.getNumber()+" ");
        }
        return sb.toString();
    }

    private void checkDeliveryRange(AddressBook addressBook) {
        String shopAddress = shopProperties.getAddress();
        String userAddress = getFullAddress(addressBook);
        String ak = baiduProperties.getAk();

        if (isBlank(shopAddress) || isBlank(userAddress) || isBlank(ak)) {
            log.warn("配送距离校验配置不完整，降级放行下单。shopAddress={}, userAddressExists={}, akExists={}",
                    shopAddress, !isBlank(userAddress), !isBlank(ak));
            return;
        }

        try {
            String shopLocation = geocoding(shopAddress, ak);
            String userLocation = geocoding(userAddress, ak);
            if (isBlank(shopLocation) || isBlank(userLocation)) {
                log.warn("地址坐标解析失败，降级放行下单。shopLocation={}, userLocation={}", shopLocation, userLocation);
                return;
            }

            Integer distance = getRidingDistance(shopLocation, userLocation, ak);
            if (distance == null) {
                log.warn("骑行距离获取失败，降级放行下单。shopLocation={}, userLocation={}", shopLocation, userLocation);
                return;
            }

            if (distance > MAX_DELIVERY_DISTANCE_METERS) {
                throw new OrderBusinessException(MessageConstant.ADDRESS_OUT_OF_RANGE);
            }
        } catch (OrderBusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("配送距离校验异常，降级放行下单。", ex);
        }
    }

    private String geocoding(String address, String ak) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("address", address);
        paramMap.put("output", "json");
        paramMap.put("ak", ak);

        String response = HttpClientUtil.doGet(BAIDU_GEOCODING_API, paramMap);
        if (isBlank(response)) {
            return null;
        }

        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject == null || jsonObject.getInteger("status") == null || jsonObject.getInteger("status") != 0) {
            return null;
        }

        JSONObject result = jsonObject.getJSONObject("result");
        if (result == null) {
            return null;
        }

        JSONObject location = result.getJSONObject("location");
        if (location == null) {
            return null;
        }

        Double lat = location.getDouble("lat");
        Double lng = location.getDouble("lng");
        if (lat == null || lng == null) {
            return null;
        }
        return lat + "," + lng;
    }

    private Integer getRidingDistance(String origin, String destination, String ak) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("origin", origin);
        paramMap.put("destination", destination);
        paramMap.put("ak", ak);

        String response = HttpClientUtil.doGet(BAIDU_DIRECTION_API, paramMap);
        if (isBlank(response)) {
            return null;
        }

        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject == null || jsonObject.getInteger("status") == null || jsonObject.getInteger("status") != 0) {
            return null;
        }

        JSONObject result = jsonObject.getJSONObject("result");
        if (result == null || result.getJSONArray("routes") == null || result.getJSONArray("routes").isEmpty()) {
            return null;
        }

        return result.getJSONArray("routes").getJSONObject(0).getInteger("distance");
    }

    private String getFullAddress(AddressBook addressBook) {
        StringBuilder sb = new StringBuilder();
        appendIfNotBlank(sb, addressBook.getProvinceName());
        appendIfNotBlank(sb, addressBook.getCityName());
        appendIfNotBlank(sb, addressBook.getDistrictName());
        appendIfNotBlank(sb, addressBook.getDetail());
        return sb.toString();
    }

    private void appendIfNotBlank(StringBuilder sb, String value) {
        if (!isBlank(value)) {
            sb.append(value);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }


}
