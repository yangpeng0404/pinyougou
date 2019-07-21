package com.pinyougou.sellergoods.service;
import java.util.List;
import com.pinyougou.pojo.TbSeller;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SellerService extends CoreService<TbSeller> {


	/**
	 * 商家通过审核
	 * @param id
	 * @param status
	 */
	void updateStatus(String id,String status);
	/**
	 * 重写add
	 * @param seller
	 */
	void add(TbSeller seller);
	
	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbSeller> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbSeller> findPage(Integer pageNo, Integer pageSize, TbSeller Seller);
	
}
