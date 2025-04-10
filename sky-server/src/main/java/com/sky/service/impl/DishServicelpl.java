package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Employee;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlaverMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.StemealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServicelpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlaverMapper dishFlaverMapper;
    @Autowired
    private StemealDishMapper setmealDishMapper;
    @Autowired
    private StemealDishMapper stemealDishMapper;

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

    /**
     * 根据id删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteDish(List<Long> ids) {

        for (Long id : ids) {
            //是否有起售菜品
           Dish dish= dishMapper.selectById(id);
            if(dish.getStatus()== StatusConstant.ENABLE)
                throw(new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE));

        }
        //是否关联套餐
        List<Long> lists = stemealDishMapper.selectByIds(ids);
        if(lists!=null&&lists.size()>0){
            throw(new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL));
        }

        //删除菜品
        for(Long id:ids){
            dishMapper.deleteById(id);
        }
        //删除菜品关联味道
        for(Long id:ids){
            dishFlaverMapper.deleteByDishId(id);
        }
    }
}
