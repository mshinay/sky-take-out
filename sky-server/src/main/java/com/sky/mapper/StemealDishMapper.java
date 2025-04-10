package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StemealDishMapper {

    /**
     * 根据菜品id列表动态查询套餐
     * @param ids
     * @return
     */
    List<Long> selectByIds(List<Long> ids);
}
