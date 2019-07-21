package com.pinyougou.seckill.task;

import com.pinyougou.common.utils.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class GoodsTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    //每隔30秒执行一次
    @Scheduled(cron = "0/30 * * * * ?")
    public void pushGoods(){

        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status","1");//已审核
        criteria.andGreaterThan("stockCount",0);//库存大于0
        Date date = new Date();
        criteria.andGreaterThan("endTime",date);//大于开始时间
        criteria.andLessThan("startTime",date);//小于结束时间

        //如果以及存在redis中的商品，不要导入
        Set<Long> keys = redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).keys();
        if(keys.size()>0 && keys!=null){
            //拼接在条件
            criteria.andNotIn("id",keys);
        }
        List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(example);
        //循环存入redis
        for (TbSeckillGoods tbSeckillGood : tbSeckillGoods) {
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(tbSeckillGood.getId(),tbSeckillGood);

            //导入redis之后，还要导入每个商品的列队
            pushGoodsList(tbSeckillGood);
        }
        System.out.println("imput success ..............");
    }
    public void pushGoodsList(TbSeckillGoods goods){
        //向同一个队列中压入商品数据
        for (Integer i = 0; i < goods.getStockCount(); i++) {
            //就是作为一个库存的保存，方便下单判断，使用list，key是商品id
            //库存为多少就是多少个SIZE 值就是id即可
            redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX+goods.getId()).leftPush(goods.getId());
        }
    }

}
