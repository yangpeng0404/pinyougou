package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.MessageInfo;
import com.pinyougou.pojo.TbItem;
import entity.Result;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.TbBrandService;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "/tbBrand")
public class TbBrandController {

    @Reference
     private TbBrandService tbBrandService;

    /*
    * 未分页
    * */
    @RequestMapping(value = "/findAll")
    public List<TbBrand> findAll(){

        return  tbBrandService.findAll();
    }

    /**
     * 分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/findPage")
    public PageInfo<TbBrand> findPage(
            @RequestParam(value = "pageNum",defaultValue = "1",required = false) Integer pageNum,
             @RequestParam(value = "'pageSize" ,defaultValue = "10",required = false) Integer pageSize){

        PageInfo<TbBrand> pageInfo = tbBrandService.findPage(pageNum, pageSize);
        return pageInfo;
    }

    /**
     * 分页模糊查
     * @param pageNum
     * @param pageSize
     * @param tbBrand
     * @return
     */
    @RequestMapping(value = "/search")
    public PageInfo<TbBrand> findPage(
            @RequestParam(value = "pageNum",defaultValue = "1",required = false) Integer pageNum,
            @RequestParam(value = "'pageSize" ,defaultValue = "10",required = false) Integer pageSize,
            @RequestBody  TbBrand tbBrand
    ){

        PageInfo<TbBrand> pageInfo = tbBrandService.findPage(pageNum, pageSize,tbBrand);
        return pageInfo;
    }

    /**
     * 增加
     * @param brand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            tbBrandService.add(brand);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 审查更新
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus/{status}")
    public Result updateStatus(@RequestBody Long[] ids, @PathVariable(value="status")  String status){
        try {
            tbBrandService.updateStatus(ids,status);

            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败");
        }
    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            tbBrandService.update(brand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne/{id}")
    public TbBrand findOne(@PathVariable(value = "id")Long id) {
        return tbBrandService.findOne(id);
    }

    @RequestMapping("/delete")
    public Result delete(@RequestBody Long[] ids) {
        try {
            tbBrandService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }
}
