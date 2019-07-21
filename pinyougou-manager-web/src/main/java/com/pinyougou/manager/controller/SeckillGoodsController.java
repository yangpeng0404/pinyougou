package com.pinyougou.manager.controller;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.MessageInfo;
import com.pinyougou.seckill.service.SeckillGoodsService;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillGoods;

import com.github.pagehelper.PageInfo;
import entity.Result;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {


    @Reference
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private DefaultMQProducer producer;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbSeckillGoods> findAll() {
        return seckillGoodsService.findAll();
    }


    @RequestMapping("/findPage")
    public PageInfo<TbSeckillGoods> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                             @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize) {
        return seckillGoodsService.findPage(pageNo, pageSize);
    }

    /**
     * 增加
     *
     * @param seckillGoods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbSeckillGoods seckillGoods) {
        try {
            seckillGoodsService.add(seckillGoods);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param seckillGoods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbSeckillGoods seckillGoods) {
        try {
            seckillGoodsService.update(seckillGoods);
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
    public TbSeckillGoods findOne(@PathVariable(value = "id") Long id) {
        return seckillGoodsService.findOne(id);
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
            seckillGoodsService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    @RequestMapping("/updateStatus")
    public Result updateStatus(@RequestBody Long[] ids, String status){
        try {
            for (Long id : ids) {
                TbSeckillGoods goods = new TbSeckillGoods();
                goods.setId(id);
                goods.setStatus(status);
                //简单的业务操作数据库不重写方法
                seckillGoodsService.updateByPrimaryKeySelective(goods);
            }
            if("1".equals(status)){
                //审核完后之后，发送mq消息去生产静态页面
                MessageInfo messageInfo = new MessageInfo("SECKILL_TOPIC","getHtml_Tags",ids,MessageInfo.METHOD_ADD);
                String infoStr = JSON.toJSONString(messageInfo);

                Message message = new Message(messageInfo.getTopic(),
                        messageInfo.getTags(),
                        messageInfo.getKeys(),infoStr.getBytes()
                        );
                producer.send(message);
            }


            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    @RequestMapping("/search")
    public PageInfo<TbSeckillGoods> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                             @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                             @RequestBody TbSeckillGoods seckillGoods) {
        return seckillGoodsService.findPage(pageNo, pageSize, seckillGoods);
    }

}