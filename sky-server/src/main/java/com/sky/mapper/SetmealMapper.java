package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /**
     * 根据categoryid查询setmeal表总数
     * @param id
     * @return
     */
    @Select("select count(*) from setmeal where category_id = #{id}")
    int countByCategoryId(Long id);
}
