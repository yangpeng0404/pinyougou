package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.pinyougou.common.utils.CookieUtil;
import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.mapper.*;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.UserOrder;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;


/**
 * 服务实现层
 *
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl extends CoreServiceImpl<TbOrder> implements OrderService {


    private TbOrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private TbPayLogMapper payLogMapper;


    @Autowired
    public OrderServiceImpl(TbOrderMapper orderMapper) {
        super(orderMapper, TbOrder.class);
        this.orderMapper = orderMapper;
    }



    /**
     * 重写add方法，
     *
     * @param order
     */
    @Override
    public void add(TbOrder order) {
        //获取该用户的用户id来拿到，购物车,在contorlle中设置了userID
        String userId = order.getUserId();
        //使用redis获取
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(userId);
        //log使用的orderIDlisy
        List<Long> orderIdList = new ArrayList<>();
        //循环获并且保存订单以及订单选项
        double totalMoney = 0;
        for (Cart cart : cartList) {
            TbOrder tbOrder = new TbOrder();
            long orderId = idWorker.nextId();
            orderIdList.add(orderId);
            System.out.println("订单id" + orderId);
            tbOrder.setOrderId(orderId);//订单id
            tbOrder.setPaymentType(order.getPaymentType());//支付类型
            tbOrder.setStatus("1");//状态：未付款
            tbOrder.setCreateTime(new Date());//订单创建日期
            tbOrder.setUpdateTime(new Date());//订单更新日期
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());//地址
            tbOrder.setReceiverMobile(order.getReceiverMobile());//手机号
            tbOrder.setReceiver(order.getReceiver());//收货人
            tbOrder.setSourceType(order.getSourceType());//订单来源
            tbOrder.setSellerId(userId);//商家ID
            //循环购物车明细,也就是设置购物车里面的car里面的orderItem
            //设置是付金额，要每个订单选项相加
            double money = 0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                //注意这个id也要唯一
                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(orderId);
                orderItem.setSellerId(userId);
                //金额累加
                money += orderItem.getTotalFee().doubleValue();
                orderItemMapper.insert(orderItem);
            }
            tbOrder.setPayment(new BigDecimal(money));
            totalMoney += money;
            //保存
            orderMapper.insert(tbOrder);
        }
        //当我们做好下单之后，就是生产日志，日志中包含有这次订单的订单记录号支付记录
        TbPayLog payLog = new TbPayLog();
        String out_trade_no = "" + idWorker.nextId();
        payLog.setOutTradeNo(out_trade_no);
        payLog.setCreateTime(new Date());
        //订单号列表，逗号分隔
        payLog.setOrderList(orderIdList.toString().replace("[", "").replace("]", ""));
        payLog.setPayType("1");
        //这个是总金额,一分为单位
        payLog.setTotalFee((long) (totalMoney * 100));
        payLog.setTradeState("0");//支付状态
        payLog.setUserId(order.getUserId());//用户ID
        payLogMapper.insert(payLog);//插入到支付日志表
        //放入redis中,一次支付对于一个payLog，用户名作为key
        redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);

        //删除redis中的购物数据,注意是删除小key
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }

    /**
     * 重写add方法，
     *
     * @param orderId
     */
    @Override
    public void addPayLog(Long orderId) {

        List<Long> orderIdList = new ArrayList<>();
        TbOrder order = orderMapper.selectByPrimaryKey(orderId);
        orderIdList.add(orderId);
        //当我们做好下单之后，就是生产日志，日志中包含有这次订单的订单记录号支付记录
        TbPayLog payLog = new TbPayLog();
        String out_trade_no = "" + idWorker.nextId();
        payLog.setOutTradeNo(out_trade_no);
        payLog.setCreateTime(new Date());
        //订单号列表，逗号分隔
        payLog.setOrderList(orderIdList.toString().replace("[", "").replace("]", ""));
        payLog.setPayType("1");
        //这个是总金额,一分为单位
        payLog.setTotalFee((long) (order.getPayment().doubleValue() * 100));
        payLog.setTradeState("0");//支付状态
        payLog.setUserId(order.getUserId());//用户ID
        payLogMapper.insert(payLog);//插入到支付日志表
        //放入redis中,一次支付对于一个payLog，用户名作为key
        redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);

    }


    @Override
    public PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbOrder> all = orderMapper.selectAll();
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbOrder> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }


    @Override
    public PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize, TbOrder order) {
        PageHelper.startPage(pageNo, pageSize);

        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if (order != null) {
            if (StringUtils.isNotBlank(order.getPaymentType())) {
                criteria.andLike("paymentType", "%" + order.getPaymentType() + "%");
                //criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
            }
            if (StringUtils.isNotBlank(order.getPostFee())) {
                criteria.andLike("postFee", "%" + order.getPostFee() + "%");
                //criteria.andPostFeeLike("%"+order.getPostFee()+"%");
            }
            if (StringUtils.isNotBlank(order.getStatus())) {
                criteria.andLike("status", "%" + order.getStatus() + "%");
                //criteria.andStatusLike("%"+order.getStatus()+"%");
            }
            if (StringUtils.isNotBlank(order.getShippingName())) {
                criteria.andLike("shippingName", "%" + order.getShippingName() + "%");
                //criteria.andShippingNameLike("%"+order.getShippingName()+"%");
            }
            if (StringUtils.isNotBlank(order.getShippingCode())) {
                criteria.andLike("shippingCode", "%" + order.getShippingCode() + "%");
                //criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
            }
            if (StringUtils.isNotBlank(order.getUserId())) {
                criteria.andLike("userId", "%" + order.getUserId() + "%");
                //criteria.andUserIdLike("%"+order.getUserId()+"%");
            }
            if (StringUtils.isNotBlank(order.getBuyerMessage())) {
                criteria.andLike("buyerMessage", "%" + order.getBuyerMessage() + "%");
                //criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
            }
            if (StringUtils.isNotBlank(order.getBuyerNick())) {
                criteria.andLike("buyerNick", "%" + order.getBuyerNick() + "%");
                //criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
            }
            if (StringUtils.isNotBlank(order.getBuyerRate())) {
                criteria.andLike("buyerRate", "%" + order.getBuyerRate() + "%");
                //criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
            }
            if (StringUtils.isNotBlank(order.getReceiverAreaName())) {
                criteria.andLike("receiverAreaName", "%" + order.getReceiverAreaName() + "%");
                //criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
            }
            if (StringUtils.isNotBlank(order.getReceiverMobile())) {
                criteria.andLike("receiverMobile", "%" + order.getReceiverMobile() + "%");
                //criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
            }
            if (StringUtils.isNotBlank(order.getReceiverZipCode())) {
                criteria.andLike("receiverZipCode", "%" + order.getReceiverZipCode() + "%");
                //criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
            }
            if (StringUtils.isNotBlank(order.getReceiver())) {
                criteria.andLike("receiver", "%" + order.getReceiver() + "%");
                //criteria.andReceiverLike("%"+order.getReceiver()+"%");
            }
            if (StringUtils.isNotBlank(order.getInvoiceType())) {
                criteria.andLike("invoiceType", "%" + order.getInvoiceType() + "%");
                //criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
            }
            if (StringUtils.isNotBlank(order.getSourceType())) {
                criteria.andLike("sourceType", "%" + order.getSourceType() + "%");
                //criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
            }
            if (StringUtils.isNotBlank(order.getSellerId())) {
                criteria.andLike("sellerId", "%" + order.getSellerId() + "%");
                //criteria.andSellerIdLike("%"+order.getSellerId()+"%");
            }

        }
        List<TbOrder> all = orderMapper.selectByExample(example);
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info, SerializerFeature.WriteDateUseDateFormat);
        PageInfo<TbOrder> pageInfo = JSON.parseObject(s, PageInfo.class);


        return pageInfo;
    }

    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
        return payLog;
    }

    @Override
    public void updateStatus(String out_trade_no, String transaction_id) {
        //1.修改支付日志状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");//已支付
        payLog.setTransactionId(transaction_id);//交易号
        payLogMapper.updateByPrimaryKey(payLog);
        //2.修改订单状态
        String orderList = payLog.getOrderList();//获取订单号列表
        String[] orderIds = orderList.split(",");//获取订单号数组
        //从支付日志中拿到订单id再便利，通过主键返回拿到订单修改，更新
        for (String orderId : orderIds) {
            TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            if (order != null) {
                order.setStatus("2");//已付款
                orderMapper.updateByPrimaryKey(order);
            }
        }
        //清除redis缓存数据
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }


    @Autowired
    private TbGoodsMapper goodsMapper;


    /**
     * 根据用户id,状态 查询订单集合
     * @param
     * @return
     */
    @Override
    public List<UserOrder> findOrderByUser(TbOrder order) {
        //创建一个集合存储当前用户所有订单
        List<UserOrder> userOrders = new ArrayList<>();

        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if (order != null) {

            if (StringUtils.isNotBlank(order.getStatus())) {
                criteria.andLike("status", "%" + order.getStatus() + "%");
                //criteria.andStatusLike("%"+order.getStatus()+"%");
            }

            if (StringUtils.isNotBlank(order.getUserId())) {
                criteria.andLike("userId", "%" + order.getUserId() + "%");
                //criteria.andUserIdLike("%"+order.getUserId()+"%");
            }

        }
        List<TbOrder> ordersList = orderMapper.selectByExample(example);

        if (ordersList != null && ordersList.size() > 0) {
            for (TbOrder tbOrder : ordersList) {
                UserOrder userOrder1 = new UserOrder();
                //订单创建时间
                userOrder1.setCreateTime(tbOrder.getCreateTime());
                userOrder1.setOrderId(tbOrder.getOrderId());
                userOrder1.setSellerId(tbOrder.getSellerId());

                //订单状态
                userOrder1.setStatus(tbOrder.getStatus());

                userOrder1.setPayment(tbOrder.getPayment());

                //商品订单信息
                TbOrderItem whereOrderItem = new TbOrderItem();
                whereOrderItem.setOrderId(tbOrder.getOrderId());
                List<TbOrderItem> orderItems = orderItemMapper.select(whereOrderItem);
                userOrder1.setOrderItemList(orderItems);
                for (TbOrderItem orderItem : orderItems) {
                    TbGoods tbGoods = new TbGoods();
                    tbGoods.setId(orderItem.getGoodsId());
                    TbGoods tbGoods1 = goodsMapper.selectByPrimaryKey(tbGoods);
                    if (tbGoods1 != null) {
                        String goodsName = tbGoods1.getGoodsName();
                        userOrder1.setGoodsName(goodsName);
                    }
                }
                //添加进集合
                userOrders.add(userOrder1);
            }
        }
        return userOrders;
    }
}
