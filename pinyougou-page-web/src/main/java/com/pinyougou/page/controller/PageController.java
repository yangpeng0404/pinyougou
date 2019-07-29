package com.pinyougou.page.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.page.service.ItemPageService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/page")
public class PageController {

    @Reference
    private ItemPageService itemPageService;

    private LinkedList goodsIds=new LinkedList();

    @RequestMapping("/footmark")
    public void footmark(@RequestParam String URL){
        System.out.println("itemPageService===:"+itemPageService);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String goodsIdstr = URL.split("9105/")[1].split("\\.")[0];
        System.out.println("str"+goodsIdstr);
        Long goodId = new Long(goodsIdstr);

            //如果长度为0,添加
            if (goodsIds.size()==0){
                goodsIds.add(goodId);
            }else {
                if (goodsIds.contains(goodId)){
                    System.out.println("id已经在集合中,删除再添加在第一个位置");

                    //清除集合中数据
                    goodsIds.remove(goodId);
                    goodsIds.addFirst(goodId);
                    //清除缓存中数据
                    itemPageService.qingchu(goodId);
                }else {
                    //不存在,添加到集合中,添加到第一个位置
                    goodsIds.addFirst(goodId);
                }
            }

            if (goodsIds.size()>10){
                //移除最后一个元素
                goodsIds.removeLast();
            }


        System.out.println("ids"+goodsIds);
        if (!"anonymousUser".equals(username)){
            //如果用户已经登录
            //存入redis
            itemPageService.footmark(username,goodsIds);
            System.out.println("redis");
        }else {
            //存入cookie
            System.out.println("cookie");
        }
    }

}
