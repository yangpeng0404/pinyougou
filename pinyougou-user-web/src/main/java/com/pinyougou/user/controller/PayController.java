package com.pinyougou.user.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    //注入订单服务，做订单状态更新
    @Reference
    private OrderService orderService;

    /**获取验证码
     * 无参数
     * 返回值：map 有支付单号id，总金额，支付二维码code
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        //获取用户id
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //生产支付二维码，使用log中的支付id
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);

       // IdWorker idworker=new IdWorker(0,1);
        //之后传到weixin的是字符串
       // String weixinParId = idworker.nextId() + "";
        //传入金额,先设置为1分钱
       // String total_fee = "1";
         Map<String,String>  resultMap =payService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        return resultMap;
    }


    /**
     * 检查支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("queryPayStatus")
    public Result  queryPayStatus(String out_trade_no){
        try {
            int count = 0;
            //循环检测
            Map<String,String> map=null;
            while (true){

                //调用支付服务，检查
                 map = payService.queryPayStatus(out_trade_no);

                count++;
                //支付成功停止
                if ("SUCCESS".equals(map.get("trade_state"))) {
                    break;
                }
                //五秒一次 检查
                Thread.sleep(5000);

                //20次停止检查
                if(count>20){
                    return  new Result(false,"支付超时");
                }

            }
            //当支付成功后，更新订单支付状态.,需要支付id和微信支付流水号
            orderService.updateStatus(out_trade_no,map.get("transaction_id"));
            return  new Result(true,"成功！！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"支付失败");
        }

    }
}
