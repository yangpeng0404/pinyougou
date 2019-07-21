package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 根据搜索条件搜索内容展示数据返回
     * @param searchMap
     * @return
     */
    Map<String,Object> search(Map<String, Object> searchMap);

    /**
     * 更新数据到索引库中
     * @param items  就是数据
     */
    public void updateIndex(List<TbItem> items);

    /**
     *
     * @param ids
     */
    void deleteByIds(Long[] ids);

}
