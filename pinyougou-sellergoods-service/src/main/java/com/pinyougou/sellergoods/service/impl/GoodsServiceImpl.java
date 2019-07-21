package com.pinyougou.sellergoods.service.impl;
import java.util.*;

import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import entity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import tk.mybatis.mapper.entity.Example;

import com.pinyougou.sellergoods.service.GoodsService;



/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl extends CoreServiceImpl<TbGoods>  implements GoodsService {

	
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper tbSellerMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	public GoodsServiceImpl(TbGoodsMapper goodsMapper) {
		super(goodsMapper, TbGoods.class);
		this.goodsMapper=goodsMapper;
	}

	
	

	
	@Override
    public PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbGoods> all = goodsMapper.selectAll();
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbGoods> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize, TbGoods goods) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();

        //不管哪里查询good都是查到为删除的
		 criteria.andEqualTo("isDelete",false);

        if(goods!=null){
        	//注意商家id查询不能是模糊查询，改成equleTo
						if(StringUtils.isNotBlank(goods.getSellerId())){
				//criteria.andLike("sellerId","%"+goods.getSellerId()+"%");
				criteria.andEqualTo("sellerId",goods.getSellerId());
							//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
			}
			if(StringUtils.isNotBlank(goods.getGoodsName())){
				criteria.andLike("goodsName","%"+goods.getGoodsName()+"%");
				//criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(StringUtils.isNotBlank(goods.getAuditStatus())){
				criteria.andLike("auditStatus","%"+goods.getAuditStatus()+"%");
				//criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(StringUtils.isNotBlank(goods.getIsMarketable())){
				criteria.andLike("isMarketable","%"+goods.getIsMarketable()+"%");
				//criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(StringUtils.isNotBlank(goods.getCaption())){
				criteria.andLike("caption","%"+goods.getCaption()+"%");
				//criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(StringUtils.isNotBlank(goods.getSmallPic())){
				criteria.andLike("smallPic","%"+goods.getSmallPic()+"%");
				//criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(StringUtils.isNotBlank(goods.getIsEnableSpec())){
				criteria.andLike("isEnableSpec","%"+goods.getIsEnableSpec()+"%");
				//criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
	
		}
        List<TbGoods> all = goodsMapper.selectByExample(example);
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbGoods> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

	/**
	 * 添加goods
	 * @param goods
	 */
	@Override
	public void add(Goods goods) {
		//1:添加tbgoods
		TbGoods tbGoods = goods.getGoods();
		tbGoods.setAuditStatus("0");
		tbGoods.setIsDelete(false);
		goodsMapper.insert(tbGoods);
		Long tbGoodsId = tbGoods.getId();
		//2:添加goodsSesc
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		goodsDesc.setGoodsId(tbGoodsId);
		goodsDescMapper.insert(goodsDesc);
		//3.获取skuList TODO
		//抽取方法保存items
		saveItems(goods,tbGoods,goodsDesc);
	}

	@Override
	public Goods findOne(Long id) {
		//查到findTbgoods
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		//查找goodSesc
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);

		TbItem tbItem = new TbItem();
		tbItem.setGoodsId(id);
		List<TbItem> items = itemMapper.select(tbItem);

		Goods goods = new Goods();
		goods.setGoods(tbGoods);
		goods.setGoodsDesc(tbGoodsDesc);
		goods.setItemList(items);
		return goods;
	}

	/**
	 * 批量修改商品状态
	 * @param ids
	 * @param status
	 */
	@Override
	public void updateStatus(Long[] ids, String status) {
		TbGoods tbGoods = new TbGoods();
		tbGoods.setAuditStatus(status);
		//条件 创建条件将ids加进去
		Example example = new Example(TbGoods.class);
		Example.Criteria criteria = example.createCriteria();
		//条件就是 goods的id
		criteria.andIn("id", Arrays.asList(ids));
		goodsMapper.updateByExampleSelective(tbGoods,example);
		//update set status=1 where id in (12,3)
	}

	/**
	 * 通过id拿到 sku 列列表
	 * @param ids
	 * @return
	 */
	@Override
	public List<TbItem> findTbItemListByIds(Long[] ids) {
		Example example = new Example(TbItem.class);
		Example.Criteria criteria = example.createCriteria();
		criteria.andIn("goodsId",Arrays.asList(ids));
		//这里item 可能是下架的所以不能拿到下架的
		criteria.andEqualTo("status","1");
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		return tbItems;
	}

	/**
	 * 这里从写delete，原来的delete是物理删除
	 * @param ids
	 */
	@Override
	public void delete(Object[] ids){
//逻辑删除
		//update tb_goods set is_delete=1 where id in (1,2,3)
		TbGoods tbGoods = new TbGoods();
		tbGoods.setIsDelete(true);

		Example example = new Example(TbGoods.class);
		Example.Criteria criteria = example.createCriteria();
		//这边需要将objec的ids 转为long[]
		Long[] longs = new Long[ids.length];
		for (int i = 0; i < longs.length; i++) {
			longs[i]= (Long) ids[i];
		}
		criteria.andIn("id",Arrays.asList(longs));

	}

	private void saveItems(Goods goods, TbGoods goods1, TbGoodsDesc goodsDesc) {
		if("1".equals(goods1.getIsEnableSpec())) {

			//TODO
			//先获取SKU的列表
			List<TbItem> itemList = goods.getItemList();

			for (TbItem tbItem : itemList) {
				//设置页码没有保存的属性

				//设置title  SPU名 + 空格+ 规格名称 +
				String spec = tbItem.getSpec();//{"网络":"移动4G","机身内存":"16G"}
				String title = goods1.getGoodsName();
				Map map = JSON.parseObject(spec, Map.class);
				for (Object key : map.keySet()) {
					String o1 = (String) map.get(key);
					title += " " + o1;
				}
				tbItem.setTitle(title);

				//设置图片从goodsDesc中获取
				//[{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/03/wKgZhVq7N-qAEDgSAAJfMemqtP8461.jpg"}]
				String itemImages = goodsDesc.getItemImages();//

				List<Map> maps = JSON.parseArray(itemImages, Map.class);

				String url = maps.get(0).get("url").toString();//图片的地址
				tbItem.setImage(url);

				//设置分类
				TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods1.getCategory3Id());
				tbItem.setCategoryid(tbItemCat.getId());
				tbItem.setCategory(tbItemCat.getName());

				//时间
				tbItem.setCreateTime(new Date());
				tbItem.setUpdateTime(new Date());

				//设置SPU的ID
				tbItem.setGoodsId(goods1.getId());

				//设置商家
				TbSeller tbSeller = tbSellerMapper.selectByPrimaryKey(goods1.getSellerId());
				tbItem.setSellerId(tbSeller.getSellerId());
				tbItem.setSeller(tbSeller.getNickName());//店铺名

				//设置品牌明后
				TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods1.getBrandId());
				tbItem.setBrand(tbBrand.getName());
				itemMapper.insert(tbItem);
			}
		}else{
			//插入到SKU表 一条记录
			TbItem tbItem = new TbItem();
			tbItem.setTitle(goods1.getGoodsName());
			tbItem.setPrice(goods1.getPrice());
			tbItem.setNum(999);//默认一个
			tbItem.setStatus("1");//正常启用
			tbItem.setIsDefault("1");//默认的

			tbItem.setSpec("{}");


			//设置图片从goodsDesc中获取
			//[{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/03/wKgZhVq7N-qAEDgSAAJfMemqtP8461.jpg"}]
			String itemImages = goodsDesc.getItemImages();//

			List<Map> maps = JSON.parseArray(itemImages, Map.class);

			if(maps!=null && maps.size()>0){
				String url = maps.get(0).get("url").toString();//图片的地址
				tbItem.setImage(url);
			}

			//设置分类
			TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods1.getCategory3Id());
			tbItem.setCategoryid(tbItemCat.getId());
			tbItem.setCategory(tbItemCat.getName());

			//时间
			tbItem.setCreateTime(new Date());
			tbItem.setUpdateTime(new Date());

			//设置SPU的ID
			tbItem.setGoodsId(goods1.getId());

			//设置商家
			TbSeller tbSeller = tbSellerMapper.selectByPrimaryKey(goods1.getSellerId());
			tbItem.setSellerId(tbSeller.getSellerId());
			tbItem.setSeller(tbSeller.getNickName());//店铺名

			//设置品牌明后
			TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods1.getBrandId());
			tbItem.setBrand(tbBrand.getName());
			itemMapper.insert(tbItem);
		}
	}



	private void mysaveItems(Goods goods,TbGoods tbGoods,TbGoodsDesc goodsDesc){
		//先判断是否启用规格,	启用了规格在进行保存sku
		if("1".equals(tbGoods.getIsEnableSpec())){
			List<TbItem> itemList = goods.getItemList();
			//循环遍历
			for (TbItem tbItem : itemList) {
				//设置title  SPU名 + 空格+ 规格名称 +
				String spec = tbItem.getSpec();//{"网络":"移动4G","机身内存":"16G"}
				//拼接商品名 就是标题
				String title = tbGoods.getGoodsName();
				//获取这些规格选项，先将json转换为对象
				Map map = JSON.parseObject(spec, Map.class);
				for (Object ket : map.keySet()) {
					String value = (String) map.get(ket);
					title+=value+" ";
				}
				//设置标题
				tbItem.setTitle(title);
				//设置图片从goodsDesc中获取
				String itemImages = goodsDesc.getItemImages();
				//图片格式
				//[{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/03/wKgZhVq7N-qAEDgSAAJfMemqtP8461.jpg"}]


			}
		}
	}
}
