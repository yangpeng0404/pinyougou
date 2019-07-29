//package com.pinyougou.listener;
//
//import com.alibaba.fastjson.JSON;
//import com.pinyougou.page.service.ItemPageService;
//import com.pinyougou.pojo.MessageInfo;
//import com.pinyougou.pojo.TbItem;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class PageMessageListener implements MessageListenerConcurrently {
//
//    @Autowired
//    private ItemPageService pageService;
//
//    @Override
//    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
//
//        try {
//            if (msgs != null) {
//                for (MessageExt msg : msgs) {
//                    byte[] body = msg.getBody();
//                    String infoStr = new String(body);
//                    MessageInfo messageInfo = JSON.parseObject(infoStr, MessageInfo.class);
//                    //判断这个消息是要执行的方法
//                    switch (messageInfo.getMethod()) {
//                        //在es中保存和更新是一样的
//                        case 1: {//新增
//                            //这里获取的是 skulist，拿到他的goodsId 去删除
//                            String context = messageInfo.getContext().toString();
//                            List<TbItem> tbItems = JSON.parseArray(context, TbItem.class);
//                            Set<Long> set = new HashSet<>();
//                            for (TbItem tbItem : tbItems) {
//                                set.add(tbItem.getGoodsId());
//                            }
//                            for (Long aLong : set) {
//                                pageService.genItemHtml(aLong);
//                            }
//                            break;
//                        }
//                        case 2: {//更新
//                            //这里获取的是 skulist，拿到他的goodsId 去删除
//                            String context = messageInfo.getContext().toString();
//                            List<TbItem> tbItems = JSON.parseArray(context, TbItem.class);
//                            Set<Long> set = new HashSet<>();
//                            for (TbItem tbItem : tbItems) {
//                                set.add(tbItem.getGoodsId());
//                            }
//                            for (Long aLong : set) {
//                                pageService.genItemHtml(aLong);
//                            }
//                            break;
//                        }
//                        case 3: {//删除
//                            String context = messageInfo.getContext().toString();
//                            Long[] longs = JSON.parseObject(context, Long[].class);
//                            pageService.deleteById(longs);
//                            break;
//                        }
//                    }
//                }
//            }
//            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//        } catch(Exception e){
//            e.printStackTrace();
//            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//        }
//    }
//}
