package com.pinyougou.seckill.service;
import com.pinyougou.pojo.TbSeckillOrder;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService extends CoreService<TbSeckillOrder> {


	/**
	 * 使用用户id拿到秒杀对象
	 * 查询是否下单成功
	 * @param userId
	 * @return
	 */
	TbSeckillOrder queryOrderStatus(String userId);

	/**
	 * 秒杀下单（预处理订单）
	 * @param seckillId 秒杀商品的ID
	 * @param userId 下单的用户ID
	 */
	public void submitOrder(Long seckillId,String userId);
	
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize, TbSeckillOrder SeckillOrder);


	/**
	 * 支付成功更新状态，
	 * @param transaction_id
	 * @param userId
	 */
	void updateOrderStatus(String transaction_id, String userId);

	/**
	 * 删除定单并且，恢复库存
	 * @param userId
	 */
	void deleteOrder(String userId);
}
