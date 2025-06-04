package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WebSocketServer webSocketServer;


    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {

        // 异常情况处理（收货地址为空、超出配送范围、购物车为空）
        // 收货地址为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 购物车为空
        // 教学视频使用的是 List<ShoppingCart> list(ShoppingCart shoppingCart); 这难道不是查的全部的购物车数据？
        // 采用           List<ShoppingCart> listByUserId(Long userId);
        Long currentUserId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.listByUserId(currentUserId);
        if (shoppingCartList == null && !shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        // 构造订单数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        // number 订单号，UUID？
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        // 手机号
        order.setPhone(addressBook.getPhone());
        // 地址
        order.setAddress(addressBook.getDetail());
        // 收货人
        order.setCancelReason(addressBook.getConsignee());
        // 订单状态，默认？
        order.setStatus(Orders.PENDING_PAYMENT);
        // 下单用户，从ThreadLoacl获取
        order.setUserId(currentUserId);
        // 下单时间 now()
        order.setOrderTime(LocalDateTime.now());
        // 支付状态，默认？
        order.setPayStatus(Orders.UN_PAID);


        // 插入 订单 数据
        orderMapper.insert(order);

        log.info("订单id：{}",order.getId());


        // 插入 订单详细 数据
        // 构建 订单详细 列表
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);     // 将购物车的数据 复制到 订单详细 对象
            orderDetail.setOrderId(order.getId());                 // 设置 订单id
            orderDetailList.add(orderDetail);
        }
        // 批量插入 订单详细 数据
        orderDetailMapper.insertBatch(orderDetailList);

        // 清空购物车
        shoppingCartMapper.clean(currentUserId);

        // 返回数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderAmount(order.getAmount())
                .orderNumber(order.getNumber())
                .orderTime(order.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        // 查询订单
        Orders order = orderMapper.getOrderById(id);

        // 判断订单是否存在
        if (order == null){
            throw new OrderBusinessException("订单不存在");
        }

        // 发送给客户端
        Map map = new HashMap();
        map.put("type",2);      // 2 代表用户催单
        map.put("orderId",id);
        map.put("content","订单号：" + order.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));



    }
}
