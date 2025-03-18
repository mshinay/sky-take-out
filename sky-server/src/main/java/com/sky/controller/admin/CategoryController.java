package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类{}",categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result enableOrDisable(@PathVariable("status") Integer status, Long id){
        log.info("启用或者禁用{}",id);
        categoryService.enableOrDisable(status,id);
        return Result.success();
    }

    /**
     * 编辑分类信息
     * @param categoryDTO
     * @return
     */
    @PutMapping
    public Result edit(@RequestBody CategoryDTO categoryDTO){
        log.info("编辑{}",categoryDTO);
        categoryService.edit(categoryDTO);
        return Result.success();
    }
}
