package com.sky.service;

import com.sky.dto.CategoryDTO;

public interface CategoryService {

    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     */
    void enableOrDisable(Integer status, Long id);

    /**
     * 编辑分类信息
     * @param categoryDTO
     */
    void edit(CategoryDTO categoryDTO);
}
