package com.pinyougou.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.MessageInfo;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 这个类相当于一个从这里拿到消息然后工作的地方
 * 类似controller
 */
public class GoodsMessageListener implements MessageListenerConcurrently {

    @Autowired
    private ItemSearchService searchService;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            if(msgs!=null){
                //msgs是可能携带很多消息的
                for (MessageExt msg : msgs) {
                    //这个body就是消息体，将它转换回来
                    byte[] body = msg.getBody();
                    String infoStr = new String(body);
                    //转换为对象
                    MessageInfo messageInfo = JSON.parseObject(infoStr, MessageInfo.class);
                    //判断这个消息是要执行的方法
                    switch (messageInfo.getMethod()){
                        //在es中保存和更新是一样的
                        case 1:{//新增
                            String context1 =  messageInfo.getContext().toString();//获取到的是字符串
                            List<TbItem> tbItems = JSON.parseArray(context1, TbItem.class);
                            searchService.updateIndex(tbItems);
                            break;
                        }
                        case 2:{//更新
                            String context = messageInfo.getContext().toString();
                            //将他转换为对象
                            List<TbItem> tbItems = JSON.parseArray(context, TbItem.class);

                            searchService.updateIndex(tbItems);
                            break;
                        }
                        case 3:{//删除
                            String s1 = messageInfo.getContext().toString();
                            Long[] longs = JSON.parseObject(s1, Long[].class);
                            searchService.deleteByIds(longs);
                            break;
                        }

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
