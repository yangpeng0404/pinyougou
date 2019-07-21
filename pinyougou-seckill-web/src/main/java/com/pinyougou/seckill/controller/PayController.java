package com.pinyougou.seckill.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    @Reference
    private SeckillOrderService seckillOrderService;


    /**获取验证码
     * 无参数
     * 返回值：map 有支付单号id，总金额，支付二维码code
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        //获取用户id
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        //获取用户的秒杀预定单
        TbSeckillOrder order = seckillOrderService.queryOrderStatus(userId);
        if(order!=null){
            String orderId = order.getId()+"";

            String d = order.getMoney().doubleValue()*100+"";

            long money = (long) (order.getMoney().doubleValue()*100);

            String totalMoney = money+"";
            Map<String,String>  resultMap =payService.createNative(orderId,totalMoney);

            return resultMap;
        }
         return new HashMap();
    }


    /**
     * 检查支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("queryPayStatus")
    public Result  queryPayStatus(String out_trade_no){
        try {
            //获取用户id
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            int count = 0;
            //循环检测
            Map<String,String> map=null;
            while (true){

                //调用支付服务，检查
                 map = payService.queryPayStatus(out_trade_no);

                count++;
                System.out.println("count ..............."+count);
                //支付成功停止
                if ("SUCCESS".equals(map.get("trade_state"))) {
                    break;
                }
                //五秒一次 检查
                Thread.sleep(3000);

                //20次停止检查
                if(count>20){
                    //超时
                    //1：去微信发起关闭支付定单请求
                    Map closeMap =  payService.closePay(out_trade_no);
                    System.out.println("over 20 count  close payOrder success");
                    //关闭微信订单
                    if ("ORDERPAID".equals(closeMap.get("err_code"))){
                        //已经支付则更新入库
                        seckillOrderService.updateOrderStatus(map.get("transaction_id"),userId);
                    }else if ("SUCCESS".equals(map.get("result_code")) || "ORDERCLOSED".equals(map.get("err_code"))) {
                        //删除预订单三步
                        //2：删除redis
                        //3:库存加一
                        //4:库存列队压入

                        seckillOrderService.deleteOrder(userId);
                        System.out.println("time over 1 min --- delete order success");
                    }else{
                        System.out.println("weiXin error");
                    }

                    return  new Result(false,"支付超时");
                }
            }
            //当支付成功后，更新订单支付状态.,需要支付id和微信支付流水号
            //已经支付则更新入库
            //1：更新到数据库2：删除定单
            seckillOrderService.updateOrderStatus(map.get("transaction_id"),userId);
           // orderService.updateStatus(out_trade_no,map.get("transaction_id"));
            return  new Result(true,"成功！！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"支付失败");
        }

    }
}
