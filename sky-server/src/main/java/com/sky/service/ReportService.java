package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;

import java.time.LocalDate;

public interface ReportService {

    /**
     * 营业额统计
     * @param begin 开始日期
     * @param end 结束日期
     * @return 营业额统计结果
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     * @param begin 开始日期
     * @param end 结束日期
     * @return 用户统计结果
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     * @param begin 开始日期
     * @param end 结束日期
     * @return 订单统计结果
     */
    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    /**
     * 销量Top10统计
     * @param begin 开始日期
     * @param end 结束日期
     * @return 销量Top10结果
     */
    SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end);
}
