package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

    /**
     * 根据categoryid查询dish表总数
     * @param id
     * @return
     */
    @Select("select count(*) from dish where category_id = #{id}")
    int countByCategoryId(Long id);
}
