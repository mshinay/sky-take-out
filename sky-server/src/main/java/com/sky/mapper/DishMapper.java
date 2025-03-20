package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

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
}
