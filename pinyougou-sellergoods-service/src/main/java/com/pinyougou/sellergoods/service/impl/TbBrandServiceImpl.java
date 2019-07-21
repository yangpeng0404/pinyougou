package com.pinyougou.sellergoods.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.TbBrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;

@Service//创建service进入容器
public class TbBrandServiceImpl implements TbBrandService {

    //注入dao
    @Autowired
    private TbBrandMapper tbBrandMapper;


    public List<TbBrand> findAll() {
        return tbBrandMapper.selectAll();
    }

    @Override
    public PageInfo<TbBrand> findPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<TbBrand> brandList = tbBrandMapper.selectAll();
        PageInfo info = new PageInfo(brandList);
        //服务器之间传输pageInfo要序列话与反序列化，否则会报错，而且pojo必须是实现Serializable
        String s = JSON.toJSONString(info);
        PageInfo<TbBrand> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

    @Override
    public PageInfo<TbBrand> findPage(Integer pageNum, Integer pageSize, TbBrand tbBrand) {

        Example example = new Example(TbBrand.class);
        Example.Criteria criteria = example.createCriteria();

        if (tbBrand!=null) {
            if (StringUtils.isNotBlank(tbBrand.getName())) {
                criteria.andLike("name","%"+tbBrand.getName()+"%");
            }
            if (StringUtils.isNotBlank(tbBrand.getFirstChar())) {
                criteria.andLike("FirstChar","%"+tbBrand.getFirstChar()+"%");
            }
        }

        PageHelper.startPage(pageNum,pageSize);
        List<TbBrand> brandList = tbBrandMapper.selectByExample(example);
        PageInfo info = new PageInfo(brandList);
        //服务器之间传输pageInfo要序列话与反序列化，否则会报错，而且pojo必须是实现Serializable
        String s = JSON.toJSONString(info);
        PageInfo<TbBrand> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

    @Override
    public void add(TbBrand brand) {
        tbBrandMapper.insert(brand);
    }
    @Override
    public void update(TbBrand brand){
        tbBrandMapper.updateByPrimaryKey(brand);
    }
    /**
     * 根据ID获取实体
     * @param id
     * @return
     */
    @Override
    public TbBrand findOne(Long id){
        return tbBrandMapper.selectByPrimaryKey(id);
    }

    /**
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        Example example = new Example(TbBrand.class);
        Example.Criteria criteria = example.createCriteria();

        //这个拼接相当于 where id in  （ids），不过这个参数类型是集合，所以的转换
        criteria.andIn("id", Arrays.asList(ids));
        tbBrandMapper.deleteByExample(example);
    }
}
