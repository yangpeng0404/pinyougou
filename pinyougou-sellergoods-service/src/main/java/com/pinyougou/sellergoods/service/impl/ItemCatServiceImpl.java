package com.pinyougou.sellergoods.service.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;  

import com.pinyougou.sellergoods.service.ItemCatService;



/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl extends CoreServiceImpl<TbItemCat>  implements ItemCatService {

	
	private TbItemCatMapper itemCatMapper;

    @Autowired
    private RedisTemplate redisTemplate;

	@Autowired
	public ItemCatServiceImpl(TbItemCatMapper itemCatMapper) {
		super(itemCatMapper, TbItemCat.class);
		this.itemCatMapper=itemCatMapper;
	}

	
	

	
	@Override
    public PageInfo<TbItemCat> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbItemCat> all = itemCatMapper.selectAll();
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbItemCat> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbItemCat> findPage(Integer pageNo, Integer pageSize, TbItemCat itemCat) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();

        if(itemCat!=null){			
						if(StringUtils.isNotBlank(itemCat.getName())){
				criteria.andLike("name","%"+itemCat.getName()+"%");
				//criteria.andNameLike("%"+itemCat.getName()+"%");
			}
	
		}
        List<TbItemCat> all = itemCatMapper.selectByExample(example);
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbItemCat> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    @Override
    public List<TbItemCat> findParentId(Long parentId) {
	    //在分类管理里面，都会调用初始化findyID
        //所以每次做了什么操作都要先更新缓存

        TbItemCat tbitemCat = new TbItemCat();
        tbitemCat.setParentId(parentId);

        //每次执行查询的时候，一次性读取缓存进行存储 (因为每次增删改都要执行此方法)

        //拿到所有分类
        List<TbItemCat> list = findAll();
        for(TbItemCat itemCat:list){
            //存入redis
            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
        }
        return itemCatMapper.select(tbitemCat);
    }

    @Override
    public List<Map<String,Object>> findtwothree(Long parentId) {
        TbItemCat cat = new TbItemCat();
        cat.setParentId(parentId);
        List<Map<String,Object>> list = new ArrayList<>();
        List<TbItemCat> tbItemCats = itemCatMapper.select(cat);
        for (TbItemCat tbItemCat : tbItemCats) {
            Map<String,Object> two = new HashMap<>();
            two.put("twoCat",tbItemCat);
            List<TbItemCat> threeList = new ArrayList<>();
            cat.setParentId(tbItemCat.getId());
            List<TbItemCat> newthreelist = itemCatMapper.select(cat);
            for (TbItemCat threeitemCat : newthreelist) {
                threeList.add(threeitemCat);
            }
            two.put("threeCatList",threeList);
            list.add(two);
        }
        return list;
    }

}
