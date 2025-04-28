package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

public interface ShoppingCartService {
    /**
     * 往购物车里添加菜品或者套餐
     * @param shoppingCartDTO
     * @return
     */
    void addToShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
