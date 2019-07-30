package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.sellergoods.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbSeckillOrder> findAll() {
        return seckillOrderService.findAll();
    }


    @RequestMapping("/findPage")
    public PageInfo<TbSeckillOrder> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                             @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize) {
        return seckillOrderService.findPage(pageNo, pageSize);
    }

    @RequestMapping("/search")
    public PageInfo<TbSeckillOrder> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                             @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                             @RequestBody TbSeckillOrder seckillOrder) {
        return seckillOrderService.findPage(pageNo, pageSize, seckillOrder);
    }

}
