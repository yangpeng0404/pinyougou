package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import entity.Result;
import com.pinyougou.pojo.TbAddress;

import com.pinyougou.user.service.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/address")
public class AddressController {

	@Reference
	private AddressService addressService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbAddress> findAll(){
		return addressService.findAll();
	}
	
	
	
	@RequestMapping("/findPage")
    public PageInfo<TbAddress> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                        @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize) {
        return addressService.findPage(pageNo, pageSize);
    }
	
	/**
	 * 增加
	 * @param address
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbAddress address){
		try {
			addressService.add(address);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param address
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbAddress address){
		try {
			addressService.update(address);
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
	public TbAddress findOne(@PathVariable(value = "id") Long id){
		return addressService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete/{id}")
	public Result delete(@PathVariable(value = "id") Long id){
		try {
			addressService.delete(id);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	

	@RequestMapping("/search")
    public PageInfo<TbAddress> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                        @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                        @RequestBody TbAddress address) {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		address.setUserId(name);
		return addressService.findPage(pageNo, pageSize, address);
    }
	
}
