package com.pinyougou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.MessageInfo;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.seckill.service.impl.SeckillOrderServiceImpl;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

public class DelayMessageListener implements MessageListenerConcurrently {
    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

   @Autowired
   private SeckillOrderService seckillOrderService;


    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            if (msgs != null) {
                for (MessageExt msg : msgs) {
                    System.out.println("shou dao message");
                    byte[] body = msg.getBody();
                    String s = new String(body);
                    MessageInfo messageInfo = JSON.parseObject(s, MessageInfo.class);

                    //订单更新 标识即可
                    if (messageInfo.getMethod() == MessageInfo.METHOD_UPDATE) {


                        //获取Redis中的未支付订单的信息
                        TbSeckillOrder tbSeckillOrder = JSON.parseObject(messageInfo.getContext().toString(), TbSeckillOrder.class);

                        //获取数据库中的订单的信息 ,如果数据库没有改定单，就说明没有支付
                        TbSeckillOrder seckillOrder = seckillOrderMapper.selectByPrimaryKey(tbSeckillOrder.getId());
                        if (seckillOrder == null) {
                            //关闭微信订单  如果 关闭微信订单的时候出现该订单已经关闭 则说明不需要 再恢复库存

                            //删除订单
                            //注意这里，可能以经删除过，所以方法里必须要有非空判断
                            seckillOrderService.deleteOrder(tbSeckillOrder.getUserId());
                            System.out.println("time over MQmessage min delete order success");
                        }
                        //有订单 无需关心
                    }

                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
