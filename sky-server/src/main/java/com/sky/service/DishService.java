package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     */
    void saveDish(DishDTO dishDTO);

    /**
     * 菜品分类查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteDish(List<Long> ids);

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    void updataDish(DishDTO dishDTO);

    /**
     * 通过id查询菜品
     * @param id
     * @return
     */
    DishVO getInfoById(Long id);

    /**
     * 通过类型查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> queryByCategory(String categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
