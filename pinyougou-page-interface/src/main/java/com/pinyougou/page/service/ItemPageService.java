package com.pinyougou.page.service;

import java.util.LinkedList;

/**
 * 商品详细页接口
 * @author Administrator
 *
 */
public interface ItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId
     */
    public void genItemHtml(Long goodsId);


    /**
     * 删除 页面
     * @param goodsId
     */
    public void deleteById(Long[] goodsId);


    //我的足迹
    public void footmark(String username, LinkedList goodsIds);

    //我的足迹-清除redis
    public void qingchu(Long goodId);
}
