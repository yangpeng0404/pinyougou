package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.ItemCatService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

	@Reference
	private ItemCatService itemCatService;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbItemCat> findAll(){
		return itemCatService.findAll();
	}



	@RequestMapping("/findPage")
	public PageInfo<TbItemCat> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
										@RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize) {
		return itemCatService.findPage(pageNo, pageSize);
	}

	/**
	 * 申请添加分类列表
	 * @param
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Map<String,TbItemCat> itemCatMap){
		try {
			Long itemCatId = getItemCatId(itemCatMap.get("itemCat1List"),0L);
			Long itemCatId1 = getItemCatId(itemCatMap.get("itemCat2List"), itemCatId);
			Long itemCatId2 = getItemCatId(itemCatMap.get("itemCat3List"), itemCatId1);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 添加分类进数据库
	 * @param itemCat 分类
	 * @param id 分类id
	 * @return
	 */
	private Long getItemCatId(TbItemCat itemCat,Long id){
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();

		List<TbItemCat> select = itemCatService.select(itemCat);
		if ( select!= null && select.size() != 0) {
			for (TbItemCat tbItemCat : select) {
				return tbItemCat.getId();
			}
		}else {
			itemCat.setSellerId(sellerId);
			itemCat.setStatus("0");
			itemCat.setParentId(id);
			itemCatService.add(itemCat);
			List<TbItemCat> itemCats = itemCatService.select(itemCat);
			if (itemCats != null) {
				for (TbItemCat cat : itemCats) {
					return cat.getId();
				}
			}
		}

		throw new RuntimeException("添加异常");
	}

	/**
	 * 修改
	 * @param itemCat
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbItemCat itemCat){
		try {
			itemCatService.update(itemCat);
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
	public TbItemCat findOne(@PathVariable(value = "id") Long id){
		return itemCatService.findOne(id);
	}

	@RequestMapping("/findParentId/{parentId}")
	public List<TbItemCat> findParentId(@PathVariable("parentId") Long parentId){
		return itemCatService.findParentId(parentId);
	}

	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(@RequestBody Long[] ids){
		try {
			itemCatService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}



	@RequestMapping("/search")
	public PageInfo<TbItemCat> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
										@RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
										@RequestBody TbItemCat itemCat) {
		return itemCatService.findPage(pageNo, pageSize, itemCat);
	}

}
