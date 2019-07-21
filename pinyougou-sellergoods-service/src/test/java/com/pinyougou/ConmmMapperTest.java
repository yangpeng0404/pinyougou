package com.pinyougou;

import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-dao.xml")
public class ConmmMapperTest {

    @Autowired
    private TbBrandMapper tbBrandMapper;

    @Test
    public void insert() {
        //插入的是一样的
        TbBrand tbBrand = new TbBrand();
        tbBrand.setName("西八");
        tbBrandMapper.insert(tbBrand);
    }

    @Test
    public void delete() {
        tbBrandMapper.deleteByPrimaryKey(32L);
    }

    @Test
    public void update() {
        TbBrand tbBrand = new TbBrand();
        tbBrand.setName("冬瓜");
        tbBrand.setId(32l);
        //根据主键修改
        tbBrandMapper.updateByPrimaryKey(tbBrand);
    }

    @Test
    public void select() {
        //通过id查找
        TbBrand tbBrand = tbBrandMapper.selectByPrimaryKey(32l);
        System.out.println(tbBrand);
    }

    /*
     * 公用类的条件查询
     * */
    @Test
    public void example() {
        //他这个和普通类查询差不多
        //pojo对象作为条件. 对象作为条件的话各个属于是用and=拼接
        TbBrand tbBrand = new TbBrand();
        tbBrand.setName("冬瓜");
        // List<TbBrand> list = tbBrandMapper.select(tbBrand);
        //这个相当于

    }

    /*
     * 公用mapper使用mapper查询
     * */
    @Test
    public void example1() {
        //创建一个条件对象，参数传入表名，传入pojo就可以，里面已经有了映射

        Example example = new Example(TbBrand.class);
        //如果我要查询所好几个id的结果，拼接条件要加andIn
        Example.Criteria criteria = example.createCriteria();
        List<Long> ids  = new ArrayList<>();
        ids.add(1l);
        ids.add(2l);
        ids.add(3l);
        //拼接条件,字段名加值
        criteria.andIn("id",ids);
        List<TbBrand> tbBrands = tbBrandMapper.selectByExample(example);
        System.out.println(tbBrands);
    }

}
