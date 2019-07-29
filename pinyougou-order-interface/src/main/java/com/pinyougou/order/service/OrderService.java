package com.pinyougou.order.service;
import com.pinyougou.pojo.TbOrder;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.UserOrder;



/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface OrderService extends CoreService<TbOrder> {



	void addPayLog(Long orderId);

	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize, TbOrder Order);


	/**
	 * 从redis中获取支付log
	 * @param userId
	 * @return
	 */
	TbPayLog searchPayLogFromRedis(String userId);

	/**
	 * 支付成功更新状态
	 * @param out_trade_no
	 * @param transaction_id
	 */
	void updateStatus(String out_trade_no, String transaction_id);



    /****
     * 查询所有订单信息
     * @return
     */
    List<TbOrder> findAll();








	/**
	 * 用户ID,状态 查询订单
	 * @param
	 * @return
	 */
	List<UserOrder> findOrderByUser(TbOrder order);
}
