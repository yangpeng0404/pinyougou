package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pageDir}")
    private String pageDir;

    @Autowired
    private TbGoodsMapper tbGoodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;


    @Autowired
    private FreeMarkerConfigurer configurer;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 通过 goodsId 来创建静态页面
     * @param goodsId
     */
    @Override
    public void genItemHtml(Long goodsId) {
        //查询数据库的商品的数据   生成静态页面

        //1.根据SPU的ID 查询商品的信息（goods  goodsDesc  ）
        TbGoods tbGoods = tbGoodsMapper.selectByPrimaryKey(goodsId);
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);

        //2.使用freemarker 创建模板  使用数据集 生成静态页面 (数据集 和模板)
        genHTML("item.ftl", tbGoods, tbGoodsDesc);
    }

    @Override
    public void deleteById(Long[] goodsId) {
        try {
            for (Long aLong : goodsId) {
                FileUtils.forceDelete(new File(pageDir + aLong + ".html"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建页面
     * @param templateName
     * @param tbGoods
     * @param tbGoodsDesc
     */
    private void genHTML(String templateName, TbGoods tbGoods, TbGoodsDesc tbGoodsDesc) {
        FileWriter writer =null;

        try {
            //1.创建一个configuration对象
            //2.设置字符编码 和 模板加载的目录
            Configuration configuration = configurer.getConfiguration();
            //3.获取模板对象
            Template template = configuration.getTemplate(templateName);

            //4.获取数据并且存入model
            Map model = new HashMap();
            model.put("tbGoods", tbGoods);
            model.put("tbGoodsDesc", tbGoodsDesc);

            //根据分类的ID 查询分类的对象
            TbItemCat tbItemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id());
            TbItemCat tbItemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id());
            TbItemCat tbItemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            model.put("itemCat1",tbItemCat1.getName());
            model.put("itemCat2",tbItemCat2.getName());
            model.put("itemCat3",tbItemCat3.getName());
            //查询商品SPU的对应的所有的SKU的列表数据
            //select * from tb_item where goods_id=1 and status=1 order by is_default desc

            Example exmaple = new Example(TbItem.class);
            Example.Criteria criteria = exmaple.createCriteria();
            criteria.andEqualTo("goodsId",tbGoods.getId());
            criteria.andEqualTo("status","1");
            exmaple.setOrderByClause("is_default desc");//order by  is_default desc

            List<TbItem> tbItems = itemMapper.selectByExample(exmaple);

            model.put("skuList",tbItems);

            //5.创建一个写流，就是你要生成的文件留
            //路径写在配置文件之后只要改变 配置文件路径就可以创建页面
            writer = new FileWriter(new File(pageDir + tbGoods.getId() + ".html"));
            //6.调用模板对象的process 方法输出到指定的文件中
            template.process(model, writer);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
