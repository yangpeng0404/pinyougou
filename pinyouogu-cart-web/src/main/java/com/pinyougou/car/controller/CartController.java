package com.pinyougou.car.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.car.service.CartService;
import com.pinyougou.common.utils.CookieUtil;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbUser;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author 鹏哥的包子
 * @version 1.0
 * @package com.pinyougou.cart.controller *
 * @since 1.0
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    /**
     * 获取购物车的列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) {

        //考虑是否登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if ("anonymousUser".equals(username)) {
            System.out.println("cookie");
            //说明是匿名登录，就是未登录
            String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            //如果cookie中没有的话，就给他一个空，但是不能为null
            if (StringUtils.isEmpty(cartListString)) {
                cartListString = "[]";
            }
            List<Cart> cookieCartList = JSON.parseArray(cartListString, Cart.class);
            return cookieCartList;

        } else {
            System.out.println("redis");
            //是登录状态
            //操作redis
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            if (cartListFromRedis == null) {
                cartListFromRedis=new ArrayList<Cart>();
            }
            //如果走到这里，那么代表是登录了，不管是第几次登陆，这个时候就要合并
            //获取cookCar
            String cookieValue = CookieUtil.getCookieValue(request, "cartList","UTF-8");
            if(StringUtils.isEmpty(cookieValue)){
                cookieValue="[]";
            }
            List<Cart> cookieCarList = JSON.parseArray(cookieValue, Cart.class);
            //何必购物车
            List<Cart> cartListConmm = cartService.commMarge(cookieCarList,cartListFromRedis);
            //将新的car存入redis
            cartService.saveCartListToRedis(username,cartListConmm);
            //清除 cookie中的car
            CookieUtil.deleteCookie(request,response,"cartList");
            if (cartListConmm==null){
                cartListConmm= new ArrayList<Cart>();
            }
            //返回最新的car
            return cartListConmm;
        }
    }


    /**
     * 添加商品到已有的购物车的列表中
     *
     * @param itemId
     * @param num
     * @return
     */

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")//注解方式
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//统一指定的域访问我的服务器资源
        //response.setHeader("Access-Control-Allow-Credentials", "true");//同意客户端携带cookie
        try {
            //find方法主要是做获取，add要做添加，也要判断是否登录
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(name)) {
                //为登录
                List<Cart> cartList = findCartList(request,response);//获取购物车列表
                cartList = cartService.addGoodsToCartList(cartList, itemId, num);
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            } else {
                //以登录
                List<Cart> cartList = findCartList(request,response);//获取购物车列表
                cartList = cartService.addGoodsToCartList(cartList,itemId,num);
                cartService.saveCartListToRedis(name,cartList);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }
}
