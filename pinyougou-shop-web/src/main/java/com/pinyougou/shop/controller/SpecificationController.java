package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.sellergoods.service.SpecificationService;
import entity.Result;
import entity.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/specification")
public class SpecificationController {

	@Reference
	private SpecificationService specificationService;
	

	/**
	 * 增加
	 * @param specification
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbSpecification specification){
		try {
			List<TbSpecification> select = specificationService.select(specification);
			if ( select!= null && select.size() != 0) {
				return new Result(false,"已有该品牌");
			}
			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			specification.setSellerId(name);
			specification.setStatus("0");
			specificationService.add(specification);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}


	@RequestMapping("/search")
	public PageInfo<TbSpecification> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
											  @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
											  @RequestBody TbSpecification specification) {
		return specificationService.findPage(pageNo, pageSize, specification);
	}

	
}
