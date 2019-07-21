package com.pinyougou.pay.service;

import java.util.Map;

public interface PayService {

    /**
     * 生成微信支付二维码
     * @param out_trade_no 订单号
     * @param total_fee 金额(分)
     * @return
     */
     Map<String,String> createNative(String out_trade_no,String total_fee);

    /**
     * 检查支付状态
     * @param out_trade_no
     * @return
     */
    Map<String, String> queryPayStatus(String out_trade_no) throws Exception;

    /**
     * 关闭订单
     * @param out_trade_no
     * @return
     */
    Map closePay(String out_trade_no);
}
