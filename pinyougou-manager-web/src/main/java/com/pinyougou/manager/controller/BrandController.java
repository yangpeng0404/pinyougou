package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 描述
 *
 * @author pengge
 * @version 1.0
 * @package com.pinyougou.manager.controller *
 * @since 1.0
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }

    //分页查询
    @RequestMapping("/findPage")
    public PageInfo<TbBrand> findPage(
                                      @RequestParam(name="pageNo",required = true,defaultValue = "1") Integer pageNo,
                                      @RequestParam(name="pageSize",required = true,defaultValue = "10") Integer pageSize){
        return brandService.findPage(pageNo,pageSize);
    }
    //保存品牌
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
           return new Result(false,"添加失败");
        }
    }

    //数据回显 根据品牌的ID 查询品牌的数据
    @RequestMapping("/findOne/{id}")
    public TbBrand findOne(@PathVariable(name="id") Long id){
            return brandService.findOne(id);
    }
    //更新
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败");
        }
    }

    //批量删除
    @RequestMapping("/delete")
    public Result delete(@RequestBody Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    //条件分页查询
    @RequestMapping("/search")
    public PageInfo<TbBrand> findPage(
            @RequestParam(name="pageNo",required = true,defaultValue = "1") Integer pageNo,
            @RequestParam(name="pageSize",required = true,defaultValue = "10") Integer pageSize,
            @RequestBody TbBrand brand){
        return brandService.findPage(pageNo,pageSize,brand);
    }
}
