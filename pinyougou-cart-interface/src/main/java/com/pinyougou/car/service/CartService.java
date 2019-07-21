package com.pinyougou.car.service;

import com.pinyougou.pojo.Cart;

import java.util.List;

/**
 * 购物车业务
 */
public interface CartService {

    /**
     * 向已有的购物车添加商品
     * @param cartList 已有的购物车
     * @param itemId 商品的ID
     * @param num 要购买的数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);


    /**
     * 从redis中查询购物车
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 合并购物车
     * @param cookieCarList
     * @param cartListFromRedis
     * @return
     */
    List<Cart> commMarge(List<Cart> cookieCarList, List<Cart> cartListFromRedis);

}
