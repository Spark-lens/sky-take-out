package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入 订单详细 数据
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);
}
