package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//@Slf4j
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
                setmealDish.setSetmealId(setmeal.getId());
            });
            setmealDishMapper.saveDishes(setmealDTO.getSetmealDishes());
        }
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getSetmealById(Long id) {
        //根据id查询套餐
        Setmeal setmeal = setmealMapper.getById(id);
        //将查询出来的数值复制给setmealVO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        //根据id查询套餐关联菜品
        List<SetmealDish> list = setmealDishMapper.getBySetmealId(id);
        //log.info("{}",list);
        if(list!=null&&list.size()>0){

            setmealVO.setSetmealDishes(list);
        }
        return setmealVO;
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    @Override
    public void updateSetmeal(SetmealDTO setmealDTO) {
        //把前端传来的信息复制给setmeal
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //存入setmeal表
        setmealMapper.update(setmeal);
        //更新setmealDish表
        List<SetmealDish> list = setmealDishMapper.getBySetmealId(setmeal.getId());
        if(list!=null&&list.size()>0){
            //先删除原有的套餐关联菜品
            setmealDishMapper.deleteBySetmealId(setmeal.getId());
            setmealDTO.getSetmealDishes().forEach(setmealDish->{
                setmealDish.setSetmealId(setmeal.getId());
            });
            //再添加修改后的关联菜品
            setmealDishMapper.saveDishes(setmealDTO.getSetmealDishes());
        }
    }
}
