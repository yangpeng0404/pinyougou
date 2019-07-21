package com.pinyougou;

import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-dao.xml")
public class servertest {

    @Autowired
    private TbBrandMapper tbBrandMapper;

    @Test
    public void  insert(){
        TbBrand tbBrand = new TbBrand();
        tbBrand.setName("西八");
        tbBrandMapper.insert(tbBrand);
    }

    @Test
    public void  delete(){
        tbBrandMapper.deleteByPrimaryKey(32L);
    }

    @Test
    public void  update(){
        TbBrand tbBrand = new TbBrand();
        tbBrand.setName("冬瓜");
        tbBrand.setId(32l);
        //根据主键修改
        tbBrandMapper.updateByPrimaryKey(tbBrand);
    }

    @Test
    public void  select(){
        //通过id查找
        TbBrand tbBrand = tbBrandMapper.selectByPrimaryKey(32l);
        System.out.println(tbBrand);
    }

    /*
    * 条件查询
    * */
    //@Test
  /*  public void  example(){
        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo("华为");
        //查询全部
        List<TbBrand> list = tbBrandMapper.selectByExample(null);
        System.out.println(list);
        //按条件查询
        List<TbBrand> tbBrands = tbBrandMapper.selectByExample(example);
        System.out.println(tbBrands);
    }*/
}
