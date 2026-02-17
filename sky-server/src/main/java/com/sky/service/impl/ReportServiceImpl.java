package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("M-d");

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        if (begin == null || end == null || begin.isAfter(end)) {
            return TurnoverReportVO.builder()
                    .dateList("")
                    .turnoverList("")
                    .build();
        }

        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        List<Map<String, Object>> turnoverData = orderMapper.getTurnoverStatistics(beginTime, endTime, Orders.COMPLETED);

        Map<LocalDate, BigDecimal> turnoverMap = new HashMap<>();
        for (Map<String, Object> row : turnoverData) {
            LocalDate date = parseDate(row.get("orderDate"));
            BigDecimal turnover = parseBigDecimal(row.get("turnover"));
            if (date != null) {
                turnoverMap.put(date, turnover);
            }
        }

        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date.format(OUTPUT_DATE_FORMATTER));
            turnoverList.add(turnoverMap.getOrDefault(date, BigDecimal.ZERO).toString());
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        if (begin == null || end == null || begin.isAfter(end)) {
            return UserReportVO.builder()
                    .dateList("")
                    .totalUserList("")
                    .newUserList("")
                    .build();
        }

        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        Integer baseTotal = userMapper.countByCreateTimeBefore(beginTime);
        int runningTotal = baseTotal == null ? 0 : baseTotal;

        List<Map<String, Object>> newUserData = userMapper.getNewUserStatistics(beginTime, endTime);
        Map<LocalDate, Integer> newUserMap = new HashMap<>();
        for (Map<String, Object> row : newUserData) {
            LocalDate date = parseDate(row.get("createDate"));
            Integer newUserCount = parseInteger(row.get("newUserCount"));
            if (date != null) {
                newUserMap.put(date, newUserCount);
            }
        }

        List<String> dateList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            int newUserCount = newUserMap.getOrDefault(date, 0);
            runningTotal += newUserCount;
            dateList.add(date.format(OUTPUT_DATE_FORMATTER));
            totalUserList.add(String.valueOf(runningTotal));
            newUserList.add(String.valueOf(newUserCount));
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .totalUserList(String.join(",", totalUserList))
                .newUserList(String.join(",", newUserList))
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        if (begin == null || end == null || begin.isAfter(end)) {
            return OrderReportVO.builder()
                    .dateList("")
                    .orderCountList("")
                    .validOrderCountList("")
                    .totalOrderCount(0)
                    .validOrderCount(0)
                    .orderCompletionRate(0.0)
                    .build();
        }

        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        List<Map<String, Object>> orderData = orderMapper.getOrderStatistics(beginTime, endTime, Orders.COMPLETED);

        Map<LocalDate, Integer> orderCountMap = new HashMap<>();
        Map<LocalDate, Integer> validOrderCountMap = new HashMap<>();
        for (Map<String, Object> row : orderData) {
            LocalDate date = parseDate(row.get("orderDate"));
            if (date != null) {
                orderCountMap.put(date, parseInteger(row.get("orderCount")));
                validOrderCountMap.put(date, parseInteger(row.get("validOrderCount")));
            }
        }

        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            int orderCount = orderCountMap.getOrDefault(date, 0);
            int validCount = validOrderCountMap.getOrDefault(date, 0);
            totalOrderCount += orderCount;
            validOrderCount += validCount;
            dateList.add(date.format(OUTPUT_DATE_FORMATTER));
            orderCountList.add(String.valueOf(orderCount));
            validOrderCountList.add(String.valueOf(validCount));
        }

        double completionRate = totalOrderCount == 0 ? 0.0 : (double) validOrderCount / totalOrderCount;
        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(completionRate)
                .build();
    }

    private LocalDate parseDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return ((Date) value).toLocalDate();
        }
        return LocalDate.parse(value.toString(), INPUT_DATE_FORMATTER);
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
