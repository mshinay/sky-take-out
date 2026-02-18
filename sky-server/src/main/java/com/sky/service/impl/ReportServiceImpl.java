package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        if (begin == null || end == null || begin.isAfter(end)) {
            return SalesTop10ReportVO.builder()
                    .nameList("")
                    .numberList("")
                    .build();
        }

        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        List<GoodsSalesDTO> goodsSalesDTOS = orderDetailMapper.getSalesTop10(beginTime, endTime, Orders.COMPLETED);
        if (goodsSalesDTOS == null || goodsSalesDTOS.isEmpty()) {
            return SalesTop10ReportVO.builder()
                    .nameList("")
                    .numberList("")
                    .build();
        }

        List<String> nameList = new ArrayList<>();
        List<String> numberList = new ArrayList<>();
        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOS) {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(String.valueOf(goodsSalesDTO.getNumber()));
        }

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX)
        );

        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx")) {
            if (inputStream == null) {
                throw new IllegalStateException("运营数据报表模板不存在");
            }

            try (XSSFWorkbook excel = new XSSFWorkbook(inputStream);
                 ServletOutputStream out = response.getOutputStream()) {

                XSSFSheet sheet = excel.getSheet("Sheet1");

                sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

                XSSFRow row = sheet.getRow(3);
                row.getCell(2).setCellValue(businessDataVO.getTurnover());
                row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                row.getCell(6).setCellValue(businessDataVO.getNewUsers());

                row = sheet.getRow(4);
                row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

                for (int i = 0; i < 30; i++) {
                    LocalDate date = dateBegin.plusDays(i);
                    BusinessDataVO dailyBusinessData = workspaceService.getBusinessData(
                            LocalDateTime.of(date, LocalTime.MIN),
                            LocalDateTime.of(date, LocalTime.MAX)
                    );

                    row = sheet.getRow(7 + i);
                    row.getCell(1).setCellValue(date.toString());
                    row.getCell(2).setCellValue(dailyBusinessData.getTurnover());
                    row.getCell(3).setCellValue(dailyBusinessData.getValidOrderCount());
                    row.getCell(4).setCellValue(dailyBusinessData.getOrderCompletionRate());
                    row.getCell(5).setCellValue(dailyBusinessData.getUnitPrice());
                    row.getCell(6).setCellValue(dailyBusinessData.getNewUsers());
                }

                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Content-Disposition", "attachment;filename=business_report.xlsx");

                excel.write(out);
                out.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("导出运营数据报表失败", e);
        }
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
