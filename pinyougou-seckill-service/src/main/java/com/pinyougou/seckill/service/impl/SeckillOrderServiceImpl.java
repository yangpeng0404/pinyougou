package com.pinyougou.seckill.service.impl;

import java.util.Date;
import java.util.List;

import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.common.utils.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.pojo.SeckillStatus;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.seckill.thread.CreateOrderThread;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;  




/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl extends CoreServiceImpl<TbSeckillOrder>  implements SeckillOrderService {


	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;


	@Autowired
	private CreateOrderThread createOrderThread;

	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	public SeckillOrderServiceImpl(TbSeckillOrderMapper seckillOrderMapper) {
		super(seckillOrderMapper, TbSeckillOrder.class);
		this.seckillOrderMapper=seckillOrderMapper;
	}


	@Override
	public void updateOrderStatus(String transaction_id, String userId) {
		TbSeckillOrder order = (TbSeckillOrder) redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
		if(order!=null){
			order.setStatus("1");
			order.setPayTime(new Date());
			order.setTransactionId(transaction_id);
			seckillOrderMapper.insert(order);


			redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).delete(userId);
			System.out.println("pay success---update mysql and detele redisOrder success");
		}
	}

	@Override
	public void deleteOrder(String userId) {

		TbSeckillOrder order = (TbSeckillOrder) redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
		if(order!=null){
			//1:恢复库存
			//获取秒杀商品id
			Long seckillId = order.getSeckillId();
			TbSeckillGoods goods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(seckillId);
			if(goods!=null){
				//直接库存加一
				goods.setStockCount(goods.getStockCount()+1);
				redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(seckillId,goods);
			}else {
				//库存已没有，已经在数据库中了
			 TbSeckillGoods  seckillGoods =seckillGoodsMapper.selectByPrimaryKey(seckillId);
			 seckillGoods.setStockCount(seckillGoods.getStockCount()+1);

			 redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(seckillId,seckillGoods);
			}

			//2：恢复列队
			redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX+seckillId).leftPush(seckillId);


			//3:删除定单
			redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).delete(userId);

		}else {
			System.out.println("not order");
		}
		return;
	}

	@Override
	public TbSeckillOrder queryOrderStatus(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
	}

	@Override
	public void submitOrder(Long seckillId, String userId) {

		//1：判断该用户是否有订单
		if(redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId)!=null){
			throw new RuntimeException("你还有未支付的订单");
		}


		//2：做排队查询，如果说用户第一次下单创建订单需要20秒，用户再次点击订单还没创建出来
		if(redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).get(userId)!=null){
			throw new RuntimeException("你正在排队，请稍后");
		}


		//3:库存是否购，够的话减一不够抛异常，并更新到数据库
		//判断否售完不能使用这种获取的方式，高并发多线程没更新就会判断，南无就会矛盾
		/*if (seckillGoods==null || seckillGoods.getStockCount()<=0) {
			throw  new RuntimeException("商品以售完");
		}*/
		//使用redis列队判断,从队列取出，先取先得
		Long goodsId = (Long) redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + seckillId).rightPop();
		if (goodsId == null) {
			//说明商品已经没有库存了
			throw new RuntimeException("商品已被抢光");
		}

		//3：如果有商品，就可以下单，下单业务太多耗时大，使用多线程
		//将来的用户一个一个压入队列排队
		redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).leftPush(new SeckillStatus(userId,seckillId,SeckillStatus.SECKILL_queuing));

		//压入队列中后，做一个正在排队表示
		redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).put(userId,seckillId);
		//注入并且调用线程的方法
		createOrderThread.handleOrder();


	}

	@Override
    public PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbSeckillOrder> all = seckillOrderMapper.selectAll();
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSeckillOrder> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize, TbSeckillOrder seckillOrder) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if(seckillOrder!=null){			
						if(StringUtils.isNotBlank(seckillOrder.getUserId())){
				criteria.andLike("userId","%"+seckillOrder.getUserId()+"%");
				//criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getSellerId())){
				criteria.andLike("sellerId","%"+seckillOrder.getSellerId()+"%");
				//criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getStatus())){
				criteria.andLike("status","%"+seckillOrder.getStatus()+"%");
				//criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getReceiverAddress())){
				criteria.andLike("receiverAddress","%"+seckillOrder.getReceiverAddress()+"%");
				//criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getReceiverMobile())){
				criteria.andLike("receiverMobile","%"+seckillOrder.getReceiverMobile()+"%");
				//criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getReceiver())){
				criteria.andLike("receiver","%"+seckillOrder.getReceiver()+"%");
				//criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getTransactionId())){
				criteria.andLike("transactionId","%"+seckillOrder.getTransactionId()+"%");
				//criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
        List<TbSeckillOrder> all = seckillOrderMapper.selectByExample(example);
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSeckillOrder> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }



}
