package com.itheima.sms.listener;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.exceptions.ClientException;
import com.itheima.sms.utils.SmsUtil;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Map;

/**
 * 监听器 用于监听消息 调用阿里大鱼的API发送短信
 *
 * @author pengge
 * @version 1.0
 * @package com.itheima.sms.listener *
 * @since 1.0
 */
public class SMSMessageListener implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

        try {
            if(msgs!=null){
                for (MessageExt msg : msgs) {
                    byte[] body = msg.getBody();
                    //先把它转换为字符串
                    String infoStr = new String(body);
                    //发过来的是map
                    Map map = JSON.parseObject(infoStr, Map.class);
                    //创建一个uitl来发送短信

                    SmsUtil.sendSms(map);
                    System.out.println(map);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (ClientException e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }

    }
}
