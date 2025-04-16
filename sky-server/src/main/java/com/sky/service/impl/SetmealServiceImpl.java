package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 分类查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult PageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        //Page<>是由pagehelper封装的返回集合
        Page<SetmealVO> pages = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(pages.getTotal(),pages.getResult());

    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveSetmeal(SetmealDTO setmealDTO) {

        Setmeal setmeal = new Setmeal();
        //将setmealDTO信息复制给setmeal
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //将setmeal信息存入数据库
        setmealMapper.save(setmeal);
        //设置setmealDish属性，并记录字段
        if(setmealDTO.getSetmealDishes()!=null&&setmealDTO.getSetmealDishes().size()>0){
            setmealDTO.getSetmealDishes().forEach(setmealDish->{
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.saveDishes(setmealDTO.getSetmealDishes());
        }
    }
}
