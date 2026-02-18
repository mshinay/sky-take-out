package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     *
     * @param detailList
     */
    void insertBatch(List<OrderDetail> detailList);

    /**
     * 根据订单id查询详细订单信息
     * @param ordersId
     * @return
     */
    @Select("select * from order_detail where order_id = #{ordersId}")
    List<OrderDetail> getByOrdersId(Long ordersId);

    /**
     * 查询销量Top10
     * @param beginTime 开始时间（含）
     * @param endTime 结束时间（不含）
     * @param status 订单状态
     * @return 销量统计
     */
    List<GoodsSalesDTO> getSalesTop10(@Param("beginTime") LocalDateTime beginTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("status") Integer status);
}
