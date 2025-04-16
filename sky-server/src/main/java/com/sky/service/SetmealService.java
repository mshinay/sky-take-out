package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

public interface SetmealService {

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult PageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveSetmeal(SetmealDTO setmealDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    SetmealVO getSetmealById(Long id);

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    void updateSetmeal(SetmealDTO setmealDTO);
}
