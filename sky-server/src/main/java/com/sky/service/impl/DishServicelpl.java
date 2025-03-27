package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Employee;
import com.sky.mapper.DishFlaverMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        //Page<>是由pagehelper封装的返回集合
        Page<DishVO>pages = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(pages.getTotal(),pages.getResult());
    }
}
