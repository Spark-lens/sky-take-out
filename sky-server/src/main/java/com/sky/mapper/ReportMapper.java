package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface ReportMapper {
    /**
     * 获取指定日期范围内的营业额
     * @param map
     * @return
     */
    Double sunByMap(HashMap<String, Object> map);

    /**
     * 获取指定日期范围内用户数
     * @param map
     * @return
     */
    Integer getUserNumber(HashMap<String, Object> map);

    /**
     * 获取指定日期范围内订单数
     * @param map
     * @return
     */
    Integer getOrderNumber(HashMap<String, Object> map);

    /**
     * 查询销量排名 top10 的 商品、销量
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
