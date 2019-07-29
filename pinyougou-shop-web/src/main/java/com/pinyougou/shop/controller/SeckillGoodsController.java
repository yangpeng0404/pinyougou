package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemService;
import com.pinyougou.sellergoods.service.SeckillGoodsService;
import entity.Goods;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {

	@Reference
	private SeckillGoodsService seckillGoodsService;

	@Reference
	private ItemService itemService;

	/**
	 *
	 * @param goodsId
	 * @return
	 */
	@RequestMapping("/findItemsByGoodsId/{goodsId}")
	public List<TbItem> findItemsByGoodsId(@PathVariable("goodsId") Long goodsId){
		return itemService.findItemsByGoodsId(goodsId);
	}


	/**
	 * 查找商品集合
	 * @return 商品集合
	 */
	@RequestMapping("/findGoodsBySellerId")
	public List<TbGoods> findGoodsBySellerId(){
			TbGoods tbGoods = new TbGoods();
			tbGoods.setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());
			return seckillGoodsService.findGoodsBySellerId(tbGoods);

	}

	
	


	
}
