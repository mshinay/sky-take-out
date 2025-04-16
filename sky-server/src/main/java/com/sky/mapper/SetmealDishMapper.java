package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id列表动态查询套餐
     * @param ids
     * @return
     */
    List<Long> selectByIds(List<Long> ids);

    /**
     * 多条插入setmealDish表
     * @param setmealDishes
     */
    void saveDishes(List<SetmealDish> setmealDishes);
}
