package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.dao.ItemSearchDao;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemSearchDao dao;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String,Object> resultMap = new HashMap<>();

        //1.获取关键字

        String keywords = (String) searchMap.get("keywords");

        //2.创建搜索查询对象 的构建对象
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();


        String category=null;

        if(!StringUtils.isEmpty(keywords)){
            //3.创建并添加查询条件 匹配查询,
            // 如果要设置某个字段为高亮就不能用copyTo,比如我要设置title高亮它会找不到
           // searchQueryBuilder.withQuery(QueryBuilders.matchQuery("keyword",keywords));
            //multiMatchQuery就是在多个字段中都可以查到类似or
            searchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords,"seller","category","brand","title"));

            //3 聚合分组一定要有关键字才能分组
            //select category from tb_item where title like '%手机%' GROUP BY category
            //设置组合查询的分组条件
            searchQueryBuilder.addAggregation(AggregationBuilders.
                    terms("category_group").field("category").size(50));

            //过滤查询  商品分类过滤
            //过滤查询相当于and 就上 满足上面条件下满足过滤的这个
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            //由于是多个搜索的条件的过滤查询，所以我们将所有的过滤条件封装到一个多条件组合查询对象中。
            //3.1过滤 分类
            category = (String) searchMap.get("category");
            if(StringUtils.isNotBlank(category)) {
                //不为空才设置过滤
                boolQueryBuilder.filter(QueryBuilders.termQuery("category",category));
                //builder.withFilter(QueryBuilders.termQuery("category",category));
            }
            //3.2过滤 品牌
            String brand = (String) searchMap.get("brand");
            if(StringUtils.isNotBlank(brand)) {
                //不为空才设置过滤
                // builder.withFilter(QueryBuilders.termQuery("brand",brand));
                boolQueryBuilder.filter(QueryBuilders.termQuery("brand",brand));
            }
            //3.3过滤 规格
            //这里是 对象查询，字段name的写法是specMap.网络.keword
            Map<String,String> spec = (Map) searchMap.get("spec");

            if(spec!=null) {
                for (String key : spec.keySet()) {
                    //该路径上去查询
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap."+key+".keyword", spec.get(key)));
                }
            }
            //3.4过滤价格
            String price = (String) searchMap.get("price");
            if (price!=null && !"".equals(price)) {
                String[] split = price.split("-");
                if ("*".equals(split[1])) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
                }else {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0],true).to(split[1],true));
                }
            }
            searchQueryBuilder.withFilter(boolQueryBuilder);

            //把高亮设置在builder里面的标题中，这个字段不是字符串是对象
            searchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("title"))
                    .withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));

        }else {
            //匹配所有
            searchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());
        }

        //4.构建查询对象
        NativeSearchQuery searchQuery = searchQueryBuilder.build();

        //5.设置分页条件，分页条件不是过滤，加到查询条件中
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageNo==null)pageNo=1;
        if(pageSize==null)pageSize=40;
        searchQuery.setPageable(PageRequest.of(pageNo-1,pageSize));


        //6 排序条件 价格排序
        String sortField = (String) searchMap.get("sortField");
        String sortType = (String) searchMap.get("sortType");

        if(StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortType)){
            if(sortType.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, sortField);
                searchQuery.addSort(sort);
            }else if(sortType.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC, sortField);
                searchQuery.addSort(sort);
            }else{
                System.out.println("不排序");
            }
        }
        //执行查询,如果设置高亮，这样查出来的结果还是原来的，我们要自定义它返回的结果
        //一顿操作之后获取到 tbItems，结果所有数据在里面
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(searchQuery, TbItem.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();
                //hits是个封装有 [{getSourceAsString：{item:{}}，heigh:{}}]

                List<T> content = new ArrayList<>();
                //如果没有搜索到记录
                if(hits==null || hits.getHits().length<=0){
                    //那么就返回空的
                    return new AggregatedPageImpl(content);
                }


                for (SearchHit hit : hits) {
                    //sourceAsStringjson字符串
                    String sourceAsString = hit.getSourceAsString();
                    //获取item对象
                    TbItem tbItem = JSON.parseObject(sourceAsString, TbItem.class);

                    //因为之前设置了，现在从hit中拿到高亮,可以理解为，构建了高亮的时候，
                    //高亮片段只存在于hit中的heigh中二不是在sourceAsString中所以要手动取
                    //获取高亮
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    //获取高亮的域为title的高亮对象
                    HighlightField highlightField = highlightFields.get("title");

                    //然后将高亮 片段 取出设置在item中

                    if(highlightField!=null) {

                        //获取高亮的碎片
                        Text[] fragments = highlightField.getFragments();
                        StringBuffer sb = new StringBuffer();//构建高亮的数据
                        if (fragments != null) {
                            for (Text fragment : fragments) {
                                sb.append(fragment.string());
                                //获取到的高亮碎片的值<em styple="colore:red">，并且拼接
                            }
                        }
                        //不为空的时候 存储值
                        if(StringUtils.isNotBlank(sb.toString())){
                            tbItem.setTitle(sb.toString());
                        }
                    }
                    content.add((T)tbItem);




                }
                AggregatedPageImpl aggregatedPage =
                        new AggregatedPageImpl<T>(content,pageable,hits.getTotalHits(),
                                searchResponse.getAggregations(),searchResponse.getScrollId());

                return aggregatedPage;
            }
        });

        //获取分组结果
        Aggregation category_group = tbItems.getAggregation("category_group");
        // 转为StringTerms对象
        StringTerms terms = (StringTerms) category_group;
        //商品分类分组结果
        List<String> categoryList = new ArrayList<>();

        if(terms!=null){
            for (StringTerms.Bucket bucket : terms.getBuckets()) {
                //注意这里获取到的是你要拿到的字段 上面设置了filed(cateGory)
                categoryList.add(bucket.getKeyAsString());
            }
        }

        if(StringUtils.isNotBlank(category)){
            Map map = searchBrandAndSpecList(category);
            resultMap.putAll(map);
        }else if(categoryList.size()>0) {
            Map map = searchBrandAndSpecList(categoryList.get(0));
            resultMap.putAll(map);
        }

        //6.获取结果集  返回

        List<TbItem> itemList = tbItems.getContent();
        long totalElements = tbItems.getTotalElements();//总记录数
        int totalPages = tbItems.getTotalPages();//总页数
        resultMap.put("rows",itemList);
        resultMap.put("total",totalElements);
        resultMap.put("totalPages",totalPages);
        resultMap.put("categoryList",categoryList);//封装查到数据的所有分类

        return resultMap;
    }

    /**
     * 当运营商审核之后拿到，sku列表，并且用这个方法存储
     * @param items  就是数据
     */
    @Override
    public void updateIndex(List<TbItem> items) {
        //保村的时候不不要忘了把spec转为map存进去
        //先设置map 再一次性插入
        for (TbItem tbItem : items) {
            String spec = tbItem.getSpec();
            Map map = JSON.parseObject(spec, Map.class);
            tbItem.setSpecMap(map);
        }
        dao.saveAll(items);


    }

    /**
     * 根据SPU的IDs数组 进行删除
     *
     * @param ids
     */
    @Override
    public void deleteByIds(Long[] ids) {

        DeleteQuery query = new DeleteQuery();
        //删除多个goodsid
        query.setQuery(QueryBuilders.termsQuery("goodsId", ids));
        //根据删除条件 索引名 和 类型

        elasticsearchTemplate.delete(query, TbItem.class);
    }


    /**
     * 查询品牌和规格列表
     * @param category 分类名称
     * @return
     */
    //因为我们关键字查询是拿到分类名 的，我们可以使用分类名查到品牌和规格之后存到结果集中
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        //使用分类名拿到 模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //使用模板id拿到 品牌 规格
        if(typeId!=null){
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);//返回值添加品牌列表
            //根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }
}
