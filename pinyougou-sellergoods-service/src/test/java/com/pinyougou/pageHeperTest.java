package com.pinyougou;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
public class pageHeperTest {


    @Autowired
    private TbBrandMapper tbBrandMapper;

    @Test
    public void  pageTest(){

            int   pageNumber=1;
            int  pageSize = 2;

        PageHelper.startPage(pageNumber,pageSize);
        //前面写了start 后面直接查询就能分页，和elastic不一样
        //他要传入page对象，pageheper不用传参
        List<TbBrand> tbBrands = tbBrandMapper.selectAll();
        //将集合放到pageInfo中的话，就能拿到pageinfo
        PageInfo<TbBrand> pageInfo = new PageInfo<>(tbBrands);

        System.out.println("总页数："+pageInfo.getPages());
        System.out.println("每页条数："+pageInfo.getSize());
        System.out.println("总条数："+pageInfo.getTotal());
        System.out.println("list结果："+pageInfo.getList());

    }
}
