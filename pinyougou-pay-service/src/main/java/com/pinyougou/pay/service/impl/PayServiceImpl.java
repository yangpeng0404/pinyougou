package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.utils.HttpClient;
import com.pinyougou.pay.service.PayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    //注入品优购公众号的配置
    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1：创建参数
        //创建map使用weixinsdk将map转换为xml
        Map<String, String> param = new HashMap<>();
        //根据微信接口文档，以下是必填选项
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "品优购");//商品描述
        param.put("out_trade_no", out_trade_no);//商户订单号
        param.put("total_fee", total_fee);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://test.itcast.cn");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        //2：客户段发送请求
        try {
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(signedXml);
            //使用模拟客户端发请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);//设为https请求
            client.setXmlParam(signedXml);//传参
            client.post();//发送请求
            //3：获取相应结果
            String result = client.getContent();
            System.out.println(result);
            //转为map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map = new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade_no", out_trade_no);//订单号
            //4：返回
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }

    }

    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) throws Exception {
        //1：合成参数
        Map param = new HashMap();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";

        String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
        //2：使用httpClient合成请求
        HttpClient client = new HttpClient(url);
        client.setHttps(true);
        client.setXmlParam(xmlParam);
        //3:发送
        client.post();
        String result = client.getContent();
        Map<String, String> map = WXPayUtil.xmlToMap(result);
        //4：直接返回这个map
        return map;
    }

    @Override
    public Map closePay(String out_trade_no) {
        try {
            //参数设置
            Map<String,String> paramMap = new HashMap<String,String>();
            paramMap.put("appid",appid); //应用ID
            paramMap.put("mch_id",partner);    //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());//随机字符
            paramMap.put("out_trade_no",out_trade_no);   //商家的唯一编号

            //将Map数据转成XML字符
            String xmlParam = WXPayUtil.generateSignedXml(paramMap,partnerkey);

            //确定url
            String url = "https://api.mch.weixin.qq.com/pay/closeorder";

            //发送请求
            HttpClient httpClient = new HttpClient(url);
            //https
            httpClient.setHttps(true);
            //提交参数
            httpClient.setXmlParam(xmlParam);

            //提交
            httpClient.post();

            //获取返回数据
            String content = httpClient.getContent();

            //将返回数据解析成Map
            return  WXPayUtil.xmlToMap(content);

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }
}
