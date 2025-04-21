package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServicelmpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealDishMapper stemealDishMapper;

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
        if(dishDTO.getFlavors()!=null&&dishDTO.getFlavors().size()>0){
        dishDTO.getFlavors().forEach(flavor -> {
            flavor.setDishId(dish.getId());
        });
            dishFlavorMapper.insertWithDish(dishDTO.getFlavors());
        }

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
        //删除菜品关联味道
        /*for(Long id:ids){
            dishMapper.deleteById(id);
            dishFlavorMapper.deleteByDishId(id);
        }*/
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);


    }

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Override
    public void updataDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        //将DTO的属性复制给dish
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品表信息
        dishMapper.update(dish);
        //修改菜品口味表信息
        dishFlavorMapper.deleteByDishId(dish.getId());
        if(dishDTO.getFlavors()!=null&&dishDTO.getFlavors().size()>0){
        dishDTO.getFlavors().forEach(flavor -> {
            flavor.setDishId(dishDTO.getId());
        });
            dishFlavorMapper.insertWithDish(dishDTO.getFlavors());
        }

    }

    @Override
    public DishVO getInfoById(Long id) {
        DishVO dishVO = new DishVO();
        Dish dish = dishMapper.selectById(id);
        //把dish的数据复制给dishVO
        BeanUtils.copyProperties(dish, dishVO);
        List<DishFlavor> flavors=dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 通过分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> queryByCategory(String categoryId) {
        List<Dish> list = dishMapper.queryByCategory(categoryId);
        return list;
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.queryByCategory(String.valueOf(dish.getCategoryId()));

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
