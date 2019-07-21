package com.pinyougou.seckill.controller;

import java.util.List;

import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillOrder;

import com.github.pagehelper.PageInfo;
import entity.Result;

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
     * 查询下单状态
     * 无参数
     * 返回结果
     *
     * @return
     */
    @RequestMapping("/queryOrderStatus")
    public Result queryOrderStatus() {

        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(userId)) {
                return new Result(false, "403");
            } else {
                TbSeckillOrder order = seckillOrderService.queryOrderStatus(userId);
                if (order == null) {
                    return new Result(false, "正在排队中,请稍等.......");
                } else {
                    return new Result(true, "单成功！!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "异常");
        }
    }


    @RequestMapping("/submitOrder/{seckillId}")
    public Result sumbitOrder(@PathVariable(value = "seckillId") Long seckillId) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(userId)) {
                return new Result(false, "403");
            }
            seckillOrderService.submitOrder(seckillId, userId);
            //这个时候用户是不能在点击，显示排队
            return new Result(true, "正在排队中,请稍等");
        } catch (RuntimeException e) {
            //里面runtime异常也就是业务异常就使用返回异常信息的方式
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "抢单失败");
        }
    }

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

    /**
     * 增加
     *
     * @param seckillOrder
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbSeckillOrder seckillOrder) {
        try {
            seckillOrderService.add(seckillOrder);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param seckillOrder
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbSeckillOrder seckillOrder) {
        try {
            seckillOrderService.update(seckillOrder);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne/{id}")
    public TbSeckillOrder findOne(@PathVariable(value = "id") Long id) {
        return seckillOrderService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody Long[] ids) {
        try {
            seckillOrderService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }


    @RequestMapping("/search")
    public PageInfo<TbSeckillOrder> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                             @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                             @RequestBody TbSeckillOrder seckillOrder) {
        return seckillOrderService.findPage(pageNo, pageSize, seckillOrder);
    }

}
