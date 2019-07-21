package com.pinyougou.seckill.thread;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.common.utils.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.MessageInfo;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.SeckillStatus;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;

/*
* 线程类，存在线程中
* */
public class CreateOrderThread {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private DefaultMQProducer defaultMQProducer;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Async
    public void  handleOrder(){
        //模拟线程要执行业务要执行的时间，看看外面要不要等30s
        try {
            System.out.println("start create order");
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //获取订单状态对象
        SeckillStatus status= (SeckillStatus) redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).rightPop();
        //获取到 商品
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(status.getGoodsId());


        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);

        //同步到redis
        redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(status.getGoodsId(),seckillGoods);

        //3 判断判断库存是否为零，为零删除
        if(seckillGoods.getStockCount()<=0){
            //完了就更新的数据库，并删除redis
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).delete(status.getGoodsId());
        }

        //4 创建秒杀订单
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        //创建订单
        long orderId = idWorker.nextId();
        seckillOrder.setId(orderId);//设置订单的ID 这个就是out_trade_no
        seckillOrder.setCreateTime(new Date());//创建时间
        seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格  价格
        seckillOrder.setSeckillId(status.getGoodsId());//秒杀商品的ID
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setUserId(status.getUserId());//设置用户ID
        seckillOrder.setStatus("0");//状态 未支付

        //保存至redis,注意这里每个人只有一个订单，所以使用userId作为小key
        redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).put(status.getUserId(),seckillOrder);

        //下单成功删除排队标记
        redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).delete(status.getUserId());

        System.out.println("end create order");





        //下单成功发送2分钟延迟消息，看看是否支付成功
        sendMessage(seckillOrder);
    }

    private void sendMessage(TbSeckillOrder seckillOrder) {
        try {
            MessageInfo messageInfo = new MessageInfo("TOPIC_SECKILL_DELAY","TAG_SECKILL_DELAY","handleOrder_DELAY",seckillOrder,MessageInfo.METHOD_UPDATE);
            //
            Message message = new Message(messageInfo.getTopic(),messageInfo.getTags(),messageInfo.getKeys(), JSON.toJSONString(messageInfo).getBytes());
            //1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
            //设置消息演示等级 设置两分钟  实际16=30m
            message.setDelayTimeLevel(6);
            defaultMQProducer.send(message);
            System.out.println("send queryStatus by mq ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
