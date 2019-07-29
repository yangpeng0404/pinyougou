package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbSeckillGoods;
import entity.Goods;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillGoodsService  {




	/**
	 * 根据秒杀商品id查询商品
	 * @param seckillGoods
	 */
	List<TbSeckillGoods> findBySellerId(TbSeckillGoods seckillGoods);

	/**
	 * 根据商家id查询商品集合
	 * @param tbGoods
	 * @return
	 */
	List<TbGoods> findGoodsBySellerId(TbGoods tbGoods);

	void add(TbSeckillGoods seckillGoods);
}
