package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    /**
     * 根据时间区间统计，获取营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {

        // 日期列表
        ArrayList<LocalDate> dateList = getDateList(begin, end);

        // 营业额
        ArrayList<Double> turnoverList = new ArrayList<>();
        // 根据每一天的日期查询当天的营业额，并插入列表
        dateList.forEach(date ->{
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 根据时间区间，获取 HashMap ,包含开始时间和结束时间
            HashMap<String, Object> map = getMapByBeginAndEnd(beginTime, endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = reportMapper.sunByMap(map);       // 获取指定日期范围内的营业额
            turnover = (turnover == null) ? 0.0 : turnover;
            turnoverList.add(turnover);
        });

        // 数据封装
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();

        return turnoverReportVO;
    }

    /**
     * 根据时间区间统计用户数量
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 日期列表
        ArrayList<LocalDate> dateList = getDateList(begin, end);

        // 新增用户数据列表
        ArrayList<Integer> newUserList = new ArrayList<>();

        // 总用户列表
        ArrayList<Integer> totalUserList = new ArrayList<>();

        // 获取每天对应的 新增用户、总用户
        dateList.forEach(date -> {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 根据时间区间，获取 HashMap ,包含开始时间和结束时间
            HashMap<String, Object> map = getMapByBeginAndEnd(null, endTime);

            // 获取总用户数
            Integer totalUserNumber = reportMapper.getUserNumber(map);
            totalUserList.add(totalUserNumber);

            // 获取新用户数
            map.put("begin",beginTime);
            Integer newUserNumber = reportMapper.getUserNumber(map);
            newUserList.add(newUserNumber);
        });

        // 封装数据
        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();

        return userReportVO;
    }

    /**
     * 根据时间区间统计订单数量
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {

        // 日期列表
        ArrayList<LocalDate> dateList = getDateList(begin, end);

        // 每天 总订单数
        ArrayList<Integer> orderCountList = new ArrayList<>();

        // 每天 有效订单数
        ArrayList<Integer> validOrderCountList = new ArrayList<>();

        // 获取 每天的 总订单数、有效订单数
        dateList.forEach(date -> {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 根据时间区间，获取 HashMap ,包含开始时间和结束时间
            HashMap<String, Object> map = getMapByBeginAndEnd(beginTime, endTime);
            // 获取 总订单数
            Integer orderCount = reportMapper.getOrderNumber(map);
            orderCountList.add(orderCount);

            // 获取 有效订单数
            map.put("status",Orders.COMPLETED);
            Integer validOrderCount = reportMapper.getOrderNumber(map);
            validOrderCountList.add(validOrderCount);
        });

        // 订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        // 有效订单总数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        // 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

        return orderReportVO;
    }

    /**
     * 查询销量排名 top10 接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        // 设置 时间范围（LocalDate 转为 LocalDateTime格式）
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 商品名称列表
        ArrayList<String> nameList = new ArrayList<>();

        // 销量列表
        ArrayList<Integer> numberList = new ArrayList<>();

        // 查询销量排名 top10 的 商品、销量
        List<GoodsSalesDTO> salesTop10 =  reportMapper.getSalesTop10(beginTime,endTime);
        // 商品名、销量 插入数据

        /*
        // 法一：foreach
        salesTop10.forEach(goodsSalesDTO -> {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        });*/

        // 法二：stream流
        nameList = (ArrayList<String>) salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        numberList = (ArrayList<Integer>) salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();

        return salesTop10ReportVO;
    }



    /**
     * 根据时间区间，获取 LocalDate 时间列表
     * @param begin
     * @param end
     * @return
     */
    private ArrayList<LocalDate> getDateList(LocalDate begin, LocalDate end){
        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!(begin.equals(end))){
            begin = begin.plusDays(1);      // 日期计算，获取指定日期后 1天 的日期
            dateList.add(begin);
        }
        return dateList;
    }

    /**
     * 根据时间区间，获取 HashMap ,包含开始时间和结束时间
     * @param beginTime
     * @param endTime
     * @return
     */
    private HashMap<String,Object> getMapByBeginAndEnd(LocalDateTime beginTime, LocalDateTime endTime){
        HashMap<String,Object> map = new HashMap<>();
        map.put("begin",beginTime);
        map.put("end",endTime);
        return map;
    }



}
