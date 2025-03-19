package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 插入新分类
     * @param category
     */
    @Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(OperationType.INSERT)
    void insert(Category category);

    /**
     * 更新分类表信息
     * @param category
     */
    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    Page<Employee> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据类型查询分类
      * @param type
     * @return
     */
    @Select("select * from category where type = #{type}")
    List<Category> queryByType(Integer type);
}
