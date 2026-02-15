package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {


    /**
     * 根据userId,dishId或者setmealId动态查询
     * @param shoppingCart
     * @return
     */
   public List<ShoppingCart> select(ShoppingCart shoppingCart);

    /**
     * 更新菜品或者套餐的数量
     * @param shoppingCart
     */
   @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 购物车里新增菜品或者套餐
     * @param shoppingCart
     */
   @Insert("insert into shopping_cart( name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
           "values(#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime}) ")
   @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除购物车
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteAllByUserId(Long userId);

    /**
     * 根据菜品和用户id删除购物车内的物品
     * @param shoppingCart
     */
    void deleteById(ShoppingCart shoppingCart);

    /**
     * 批量插入购物车
     * @param shoppingCartList
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
