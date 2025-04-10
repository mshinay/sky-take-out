package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishFlaverMapper {

    /**
     * 将相关菜品的味道插入到味道表单
     * @param flavors
     */
    void insertWithDish(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除口味
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteByDishId(Long dishId);
}
