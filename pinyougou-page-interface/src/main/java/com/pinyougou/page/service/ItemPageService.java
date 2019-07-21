package com.pinyougou.page.service;

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
}
