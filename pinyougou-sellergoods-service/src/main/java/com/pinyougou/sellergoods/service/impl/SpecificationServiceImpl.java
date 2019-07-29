package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.List;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbTypeTemplate;
import entity.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;  

import com.pinyougou.sellergoods.service.SpecificationService;



/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl extends CoreServiceImpl<TbSpecification>  implements SpecificationService {

	
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	public SpecificationServiceImpl(TbSpecificationMapper specificationMapper) {
		super(specificationMapper, TbSpecification.class);
		this.specificationMapper=specificationMapper;
	}


    @Override
    public void delete(Long[] ids) {
        //删除规格
        Example example = new Example(TbSpecification.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        specificationMapper.deleteByExample(example);

        //删除规格关联的规格的选项
        Example exampleOption = new Example(TbSpecificationOption.class);
        //按照字段specId删除，不是主键
        exampleOption.createCriteria().andIn("specId", Arrays.asList(ids));
        specificationOptionMapper.deleteByExample(exampleOption);
    }

    /**
     * 修改规格主要是修改规格选项
     * @param specification
     */
    @Override
    public void update(Specification specification) {
        specificationMapper.updateByPrimaryKey(specification.getSpecification());
        TbSpecificationOption option= new TbSpecificationOption();
        option.setSpecId(specification.getSpecification().getId());
        //条件做为参数也可以删除
        int delete = specificationOptionMapper.delete(option);

        List<TbSpecificationOption> optionList = specification.getOptionList();
        for (TbSpecificationOption tbSpecificationOption : optionList) {
            tbSpecificationOption.setSpecId(specification.getSpecification().getId());
            specificationOptionMapper.insert(tbSpecificationOption);
        }
        //批量插入 要求 主键为ID 并且是自增才可以
        //optionMapper.insertList(optionList);
    }

    /**
     * 查找一个
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {

        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);

        //拿着id去option中进行条件查寻
        //参数是对象就是条件查询
        TbSpecificationOption option = new TbSpecificationOption();
        option.setSpecId(tbSpecification.getId());
        List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.select(option);
        Specification specification = new Specification();
        specification.setOptionList(tbSpecificationOptions);
        specification.setSpecification(tbSpecification);
        return specification;
    }

    /**
     * 添加
     * @param specification
     */
    @Override
    public void add(Specification specification) {
        //先添加规格
        TbSpecification tbSpecification = specification.getSpecification();
        specificationMapper.insert(tbSpecification);
        //拿到它的id添加选项
        for (TbSpecificationOption tbSpecificationOption : specification.getOptionList()) {
            //注意这里设置的是specId ，不要设置成SetId
            tbSpecificationOption.setSpecId(tbSpecification.getId());
            specificationOptionMapper.insert(tbSpecificationOption);
        }
    }

    @Override
    public PageInfo<TbSpecification> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbSpecification> all = specificationMapper.selectAll();
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSpecification> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbSpecification> findPage(Integer pageNo, Integer pageSize, TbSpecification specification) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();

        if(specification!=null){
            criteria.andEqualTo("status","0");
						if(StringUtils.isNotBlank(specification.getSpecName())){
				criteria.andLike("specName","%"+specification.getSpecName()+"%");
				//criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
        List<TbSpecification> all = specificationMapper.selectByExample(example);
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSpecification> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        TbSpecification tbSpecification = new TbSpecification();
        tbSpecification.setStatus(status);
        //条件 创建条件将ids加进去
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        //条件就是 goods的id
        criteria.andIn("id", Arrays.asList(ids));
        specificationMapper.updateByExampleSelective(tbSpecification,example);
    }

}
