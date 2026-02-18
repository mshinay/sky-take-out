package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openid);

    /**
     * 新增用户信息
     * @param user
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into user(openid, name, phone, sex, id_number, avatar, create_time) " +
            "values (#{openid},#{name},#{phone},#{sex},#{idNumber},#{avatar},#{createTime})")
    void insert(User user);

    /**
     * 根据id查询用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 统计某时间点之前的用户总数
     * @param beginTime 时间点
     * @return 用户总数
     */
    Integer countByCreateTimeBefore(@Param("beginTime") LocalDateTime beginTime);

    /**
     * 按天统计新增用户
     * @param beginTime 开始时间（含）
     * @param endTime 结束时间（不含）
     * @return 日期与新增用户数
     */
    List<Map<String, Object>> getNewUserStatistics(@Param("beginTime") LocalDateTime beginTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 根据动态条件统计用户数量
     * @param map 查询条件
     * @return 用户数量
     */
    Integer countByMap(Map<String, Object> map);
}
