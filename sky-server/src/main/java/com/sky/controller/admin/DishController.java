package com.sky.controller.admin;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService ;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "dishByCategory", key = "#dishDTO.categoryId")
    public Result saveDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.saveDish(dishDTO);
        return Result.success();
    }

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("查询分类表{}", dishPageQueryDTO);
        PageResult results=dishService.pageQuery(dishPageQueryDTO);
        return Result.success(results);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(cacheNames = "dishByCategory", allEntries = true)
    public Result deleteDish(@RequestParam List<Long> ids) {
        log.info("删除菜品{}",ids);
        dishService.deleteDish(ids);
        return Result.success();
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    @CacheEvict(cacheNames = "dishByCategory", allEntries = true)
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品{}", dishDTO);
        dishService.updataDish(dishDTO);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> getDishById(@PathVariable Long id) {
        log.info("根据id查询菜品信息{}",id);
        DishVO dishVO = dishService.getInfoById(id);
        return Result.success(dishVO);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> queryByCategory(String categoryId){
        log.info("查询{}相关菜品",categoryId);
        List<Dish> list = dishService.queryByCategory(categoryId);
        return Result.success(list);
    }
}
