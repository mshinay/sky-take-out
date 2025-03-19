package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServicelmpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        //默认为禁用状态
        category.setStatus(0);

        categoryMapper.insert(category);
    }

    /**
     * 启用、禁用分类
     *
     * @param status
     * @param id
     */
    @Override
    public void enableOrDisable(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.update(category);
    }

    /**
     * 编辑分类信息
     *
     * @param categoryDTO
     */
    @Override
    public void edit(CategoryDTO categoryDTO) {
        Category category = new Category();
        //springboot自带的类属性复制工具
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.update(category);
    }

    /**
     * 分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        //Page<>是由pagehelper封装的返回集合
        Page<Employee> pages = categoryMapper.pageQuery(categoryPageQueryDTO);

        return new PageResult(pages.getTotal(), pages.getResult());
    }

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    @Override
    public List<Category> query(Integer type) {
        return categoryMapper.query(type);
    }

    /**
     * 根据id删除分类
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        //查询当前分类是否关联了菜品表，如果关联了，则抛出业务异常
        int count = dishMapper.countByCategoryId(id);
        if (count > 0) {
            //当前分类关联了菜品
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //查询当前分类是否关联了套餐表，如果关联了，则抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if (count > 0) {
            //当前分类关联了套餐
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //若没关联，则删除
        categoryMapper.delete(id);

    }


}
