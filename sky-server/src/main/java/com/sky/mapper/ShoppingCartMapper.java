package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

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
}
