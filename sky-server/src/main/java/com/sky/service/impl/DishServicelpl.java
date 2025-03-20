package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishFlaverMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DishServicelpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlaverMapper dishFlaverMapper;
    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    public void saveDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //插入菜品表单
        dishMapper.insert(dish);
        //插入味道表单
        dishDTO.getFlavors().forEach(flavor -> {
            flavor.setDishId(dish.getId());
        });
        dishFlaverMapper.insertWithDish(dishDTO.getFlavors());
    }
}
