package com.pinyougou.shop.controller;

/*
        lhl
  现在是：2019/7/22
        18:14
*/

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbOrder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

   /* @RequestMapping("/findAll")
    public List<TbOrder> findAll(){
        List<TbOrder> all = orderService.findAll();
        System.out.println("执行了................");

        return all;
    }*/


    @RequestMapping("/search")
    public PageInfo<TbOrder> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                      @RequestBody TbOrder tbOrder) {
        //商家查询所有的商品，需要设置商家id,条件分页查询
        return orderService.findPage(pageNo, pageSize, tbOrder);
    }


}















