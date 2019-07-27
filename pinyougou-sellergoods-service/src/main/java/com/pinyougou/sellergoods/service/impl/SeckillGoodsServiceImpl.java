package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreServiceImpl;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.SeckillGoodsService;
import entity.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private TbGoodsMapper goodsMapper;


	@Override
	public List<TbSeckillGoods> findBySellerId(TbSeckillGoods seckillGoods) {
		Example example = new Example(TbSeckillGoods.class);
		Example.Criteria criteria = example.createCriteria();
		criteria.andEqualTo(seckillGoods.getSellerId());
		List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(example);
		return tbSeckillGoods;
	}

	@Override
	public List<TbGoods> findGoodsBySellerId(TbGoods tbGoods) {
		Example example = new Example(TbGoods.class);
		Example.Criteria criteria = example.createCriteria();
		criteria.andEqualTo(tbGoods.getSellerId());
		return goodsMapper.selectByExample(example);
	}
}
