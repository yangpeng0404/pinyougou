package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.pagehelper.PageInfo;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.MessageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.Goods;
import entity.Result;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;


	@Reference
	private ItemSearchService itemSearchService;

	@Reference
	private ItemPageService itemPageService;

	@Autowired
	private DefaultMQProducer producer;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	
	@RequestMapping("/findPage")
    public PageInfo<TbGoods> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize) {

		return goodsService.findPage(pageNo, pageSize);
    }
	
	/**
	 * 增加组合Goods
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
			goods.getGoods().setSellerId(sellerId);
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	@RequestMapping("/updateStatus/{status}")
	public Result updateStatus(@RequestBody Long[] ids, @PathVariable(value="status")  String status){
		try {
			goodsService.updateStatus(ids,status);

			//如果status是1那么就同步到es

			//审核通过之后就可以创建spu页面
				for (Long id : ids) {
					itemPageService.genItemHtml(id);
				}
			//使用中间件完成 两个处理方发送一次消息，同是删除，同时修改
			if("1".equals(status)){
				List<TbItem> items = goodsService.findTbItemListByIds(ids);
				if(items.size()>0 && items!=null){
					//itemSearchService.updateIndex(items);
					//使用中间件，服务之间互不影响

					//这里使用massageInfo 对象，发送消息
					//参数 什么商品服务 ，更新商品，方法唯一标识，消息体，哪个方法使用常量代替
					MessageInfo messageInfo = new MessageInfo("Goods_Topic","goods_update_tag","updateStatus",
		 					items,MessageInfo.METHOD_UPDATE);

					//然后使用 send方法发送过去。三个参数，主题，标签，和字节消息体
					SendResult send = producer.send(new Message(messageInfo.getTopic(),
							messageInfo.getTags(), messageInfo.getKeys(),
							JSON.toJSONString(messageInfo).getBytes()));

					System.out.println(">>>>"+send.getSendStatus());
				}
			}


			return new Result(true,"更新成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"更新失败");
		}
	}


	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbGoods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne/{id}")
	public Goods findOne(@PathVariable(value = "id") Long id){
		//查找一个 ，查的是组合good
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(@RequestBody Long[] ids){
		try {
			goodsService.delete(ids);
			//同步删除es中的item
			//itemSearchService.deleteByIds(ids);
			//使用消息中间件
			MessageInfo messageInfo = new MessageInfo("Goods_Tops","goods_delete_tag","delete",ids,MessageInfo.METHOD_DELETE);
			Message message = new Message(messageInfo.getTopic(),messageInfo.getTags(), JSON.toJSONString(messageInfo.toString()).getBytes());
			producer.send(message);

			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	

	@RequestMapping("/search")
    public PageInfo<TbGoods> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                      @RequestBody TbGoods goods) {
		//商家查询所有的商品，需要设置商家id,条件分页查询
		//goods.setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());
        return goodsService.findPage(pageNo, pageSize, goods);
    }

	
}
