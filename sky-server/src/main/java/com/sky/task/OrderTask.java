package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 订单定时处理
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;


    /**
     * 处理 支付超时 订单
     * 每分钟 检查一次是否存在支付超时订单（超时订单：下单超15分钟仍为支付），如果存在则修改订单状态为“已取消”
     */
//    @Scheduled(cron = "0 0/1 * * * ? ")
    public void processTimeoutOrder(){
        log.info("开始处理超时订单：{}",new Date());
        // 设置超时时间
        LocalDateTime timeoutTime = LocalDateTime.now().plusMinutes(-15);
        // 获取超时订单列表
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.PENDING_PAYMENT,timeoutTime);
        // 判断超时订单是否存在
        if (ordersList != null && !ordersList.isEmpty()){
            ordersList.forEach(order ->{
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时");
                order.setCancelTime(LocalDateTime.now());
                // 订单超时，修改订单
                orderMapper.update(order);
            });
        }
    }


    /**
     * 处理 派送中 状态的订单
     * 每天凌晨1点 检查一次是否存在“派送中”的订单，若存在责修改订单状态为“已完成”
     */
//    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder(){
        log.info("开始处理派送中订单：{}",new Date());

        // 凌晨一点检查 前一天的派送中订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        // 获取派送中订单列表
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS,time);
        // 判断派送中订单是否存在
        if (ordersList != null && !ordersList.isEmpty()){
            ordersList.forEach(order ->{
                order.setStatus(Orders.COMPLETED);
                order.setDeliveryTime(LocalDateTime.now());
                // 订单已完成，修改订单
                orderMapper.update(order);
            });
        }

    }

}
