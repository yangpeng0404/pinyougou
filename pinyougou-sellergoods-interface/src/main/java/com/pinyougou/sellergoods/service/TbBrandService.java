package com.pinyougou.sellergoods.service;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;

import java.util.List;

public interface TbBrandService  {

    /*
    * 查询所有
    * */
    List<TbBrand>  findAll();


    /**
     * 查询分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<TbBrand> findPage(Integer pageNum, Integer pageSize);

    /**
     * 模糊查询分页
     * @param pageNum
     * @param pageSize
     * @param tbBrand
     * @return
     */
    PageInfo<TbBrand> findPage(Integer pageNum, Integer pageSize,TbBrand tbBrand);


    /**
     * 新增品牌
     * @param brand
     */
    void add(TbBrand brand);

    /**
     * 更新品牌
     * @param brand
     */
    public void update(TbBrand brand);

    /**
     * 根据ID获取实体
     * @param id
     * @return
     */
    public TbBrand findOne(Long id);


    /**
     * 删除
     * @param ids
     */
    public  void  delete(Long[] ids);

    void updateStatus(Long[] ids, String status);
}
