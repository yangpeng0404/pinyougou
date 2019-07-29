package com.pinyougou.car.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.car.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 向购物车中添加商品
     * @param cartList 已有的购物车
     * @param itemId 商品的ID
     * @param num 要购买的数量
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1，用itemId获取sku,拿到商家id看是否有去购物车判断
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);

        String sellerId = tbItem.getSellerId();
        //2, 判断是否哟商家
        Cart cart = searchCartBySellerId(cartList,sellerId);
        //3有商家 ，向商家里面添加商品
        if(cart!=null){
            //有商家，判断是否已存在商品
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            Long tbItemId = tbItem.getId();
            TbOrderItem orderItem = searchOrderItemByItemId(orderItemList,tbItemId);
            if(orderItem!=null){
                //判断要添加的商品 是否在已有的商家的明细列表中存在  如果 有  说明要添加的商品存在于购物车中   数量向加
                orderItem.setNum(orderItem.getNum()+num);
                //金额重新计算  数量* 单价
                double v = orderItem.getNum() * orderItem.getPrice().doubleValue();
                orderItem.setTotalFee(new BigDecimal(v));//重新设置

                //判断如果商品的购买数量为0 表示不买了，就要删除商品
                if (orderItem.getNum() == 0) {
                    orderItemList.remove(orderItem);
                }
                //如果是长度为空说明 用户没购买该商家的商品就直接删除对象
                if (orderItemList.size() == 0) {//[]
                    cartList.remove(cart);//商家也删除了
                }
            }else {
                //没有直接添加商品
                TbOrderItem orderItemnew = new TbOrderItem();
                //设置他的属性
                orderItemnew.setItemId(itemId);
                orderItemnew.setGoodsId(tbItem.getGoodsId());
                orderItemnew.setTitle(tbItem.getTitle());
                orderItemnew.setPrice(tbItem.getPrice());
                orderItemnew.setNum(num);//传递过来的购买的数量
                double v = num * tbItem.getPrice().doubleValue();
                orderItemnew.setTotalFee(new BigDecimal(v));//金额
                orderItemnew.setPicPath(tbItem.getImage());//商品的图片路径

                orderItemList.add(orderItemnew);
            }

        }else {
            //4判断要添加的商品的商家的ID 是否在已有的购物车列表中存在  如果没有存在  直接添加商品
            cart = new Cart();
            cart.setSellerId(sellerId);

            cart.setSellerName(tbItem.getSeller());

            //什么都没 ，创建一个商品详细
            ArrayList<TbOrderItem> orderItems = new ArrayList<>();

            TbOrderItem orderItem = new TbOrderItem();
            //设置他的属性
            orderItem.setItemId(itemId);
            orderItem.setGoodsId(tbItem.getGoodsId());
            orderItem.setTitle(tbItem.getTitle());
            orderItem.setPrice(tbItem.getPrice());
            orderItem.setNum(num);//传递过来的购买的数量
            double v = num * tbItem.getPrice().doubleValue();
            orderItem.setTotalFee(new BigDecimal(v));//金额
            orderItem.setPicPath(tbItem.getImage());//商品的图片路径

            orderItems.add(orderItem);

            cart.setOrderItemList(orderItems);

            cartList.add(cart);

        }

        //购物车有就加没有就创建，最后一顿炒作就返回
        return cartList;
    }

    /**
     * 从redis中获取购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {

        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);

        return cartList;
    }

    /**
     * 从保存购物车到redis中
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {

        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> commMarge(List<Cart> cookieCarList, List<Cart> cartListFromRedis) {
        for (Cart cart : cookieCarList) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartListFromRedis = addGoodsToCartList(cartListFromRedis,orderItem.getItemId(),orderItem.getNum());
            }
        }
        //一顿操作后就将旧的car变成新的car
        return cartListFromRedis;
    }


    /**
     * 根据 skuid查找item
     * @param orderItemList
     * @param tbItemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long tbItemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (tbItemId==orderItem.getItemId().longValue()) {
                return  orderItem;
            }
        }
        return null;
    }

    /**
     * 通过商家id在购物车查找
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }
}
