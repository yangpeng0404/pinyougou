package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbSpecificationOption;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;  

import com.pinyougou.sellergoods.service.TypeTemplateService;



/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl extends CoreServiceImpl<TbTypeTemplate>  implements TypeTemplateService {

	
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSpecificationOptionMapper optionMapper;

	@Autowired
	public TypeTemplateServiceImpl(TbTypeTemplateMapper typeTemplateMapper) {
		super(typeTemplateMapper, TbTypeTemplate.class);
		this.typeTemplateMapper=typeTemplateMapper;
	}


    @Override
    public void updateStatus(Long[] ids, String status) {
		TbTypeTemplate tbTypeTemplate = new TbTypeTemplate();
		tbTypeTemplate.setStatus(status);
		//条件 创建条件将ids加进去
		Example example = new Example(TbGoods.class);
		Example.Criteria criteria = example.createCriteria();
		//条件就是 goods的id
		criteria.andIn("id", Arrays.asList(ids));
		typeTemplateMapper.updateByExampleSelective(tbTypeTemplate,example);
    }

    @Override
    public PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbTypeTemplate> all = typeTemplateMapper.selectAll();
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbTypeTemplate> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize, TbTypeTemplate typeTemplate) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();

        if(typeTemplate!=null){			
						if(StringUtils.isNotBlank(typeTemplate.getName())){
				criteria.andLike("name","%"+typeTemplate.getName()+"%");
				//criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(StringUtils.isNotBlank(typeTemplate.getSpecIds())){
				criteria.andLike("specIds","%"+typeTemplate.getSpecIds()+"%");
				//criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(StringUtils.isNotBlank(typeTemplate.getBrandIds())){
				criteria.andLike("brandIds","%"+typeTemplate.getBrandIds()+"%");
				//criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(StringUtils.isNotBlank(typeTemplate.getCustomAttributeItems())){
				criteria.andLike("customAttributeItems","%"+typeTemplate.getCustomAttributeItems()+"%");
				//criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
        List<TbTypeTemplate> all = typeTemplateMapper.selectByExample(example);
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbTypeTemplate> pageInfo = JSON.parseObject(s, PageInfo.class);


        //每次给分类做了变化，就要对相应的模板进行修改那么
		 //那么模板改变之后都要查询所有的方法
		 //那么redis也要更新

		 //获取模板数据
		 List<TbTypeTemplate> typeTemplateList = this.findAll();

		 //我们需要模板就是要其中的品牌以及规格
		 for (TbTypeTemplate tbTypeTemplate : typeTemplateList) {
				//将品牌转为对象
			 //存储品牌列表
			 List<Map> brandList = JSON.parseArray(tbTypeTemplate.getBrandIds(), Map.class);

			 redisTemplate.boundHashOps("brandList").put(tbTypeTemplate.getId(), brandList);

			 //存储规格列表
			 List<Map> specList = findSpecList(tbTypeTemplate.getId());//根据模板ID查询规格列表

			 redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(), specList);

			  //保存的话都是大key 和 模板id作为小key


		 }

        return pageInfo;
    }

	/**
	 * 查到规格所有信息
	 * @param id
	 * @return
	 */
	//条件是 模板id
	//模板中的specId是[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"},{"id":34,"text":"颜色"}]
	//我们将它其中的map再加一个键值对 就是List[opion]
	//查询需要返回的数据 格式时 [{'规格id':'1','规格name(text)':'网络','选项list ':'options'},{}]

	@Override
	public List<Map> findSpecList(Long id) {
		//1 通过模板id拿到模板对象
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//2:获取specId 规格id
		String specIds = tbTypeTemplate.getSpecIds();
		//转换成对象,参数时泛型类
		List<Map> maps = JSON.parseArray(specIds, Map.class);
		//4:便利maps 并且每个map封装多一个键值对 sptions
		for (Map map : maps) {
			Integer specId = (Integer) map.get("id");
			//通过specId查询 到options
			TbSpecificationOption option = new TbSpecificationOption();

			option.setSpecId(Long.valueOf(specId));

			System.out.println(option);
			List<TbSpecificationOption> options = optionMapper.select(option);
			//封装
			map.put("options",options);
		}
		return maps;
	}

}
