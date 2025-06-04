package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入 订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 获取超时订单列表
     * @param status
     * @param timeoutTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{timeoutTime}")
    List<Orders> getByStatusAndOrdertimeLT(Integer status, LocalDateTime timeoutTime);


    /**
     * 订单超时，修改订单
     * @param order
     */
    void update(Orders order);

    /**
     * 查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getOrderById(Long id);
}
