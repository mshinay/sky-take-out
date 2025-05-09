package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersHistoryQueryDTO implements Serializable {

    //页码
    private int page;

    //每页记录数
    private int pageSize;

    //订单状态
    private Integer status;
}
