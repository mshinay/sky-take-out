package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据categoryid查询dish表总数
     * @param id
     * @return
     */
    @Select("select count(*) from dish where category_id = #{id}")
    int countByCategoryId(Long id);

    /**
     * 插入菜品表单
     * @param dish
     */
    @Insert("insert into dish (name, category_id, price, image, description, create_time, update_time, create_user, update_user, status) " +
            "values (#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser}, #{status} )")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 分类查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish selectById(Long id);

    /**
     * 根据id删除菜品
     * @param id
     */
    @Delete("delete from dish where id=#{id}")
    void deleteById(Long id);

    /**
     *根据id批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);
}
