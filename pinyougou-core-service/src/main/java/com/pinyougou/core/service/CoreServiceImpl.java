package com.pinyougou.core.service;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.core.service *
 * @since 1.0
 */
public abstract class CoreServiceImpl<T> implements CoreService<T> {
    @Override
    public List<T> findAll() {
        return selectAll();
    }

    //受保护的 继承的时候可以使用到的
    protected Mapper<T> baseMapper;

    protected Class<T> clazz;

    //通过构造函数拿到子类的mapper以及，pojo类
    public CoreServiceImpl(Mapper<T> baseMapper, Class<T> clazz) {
        this.baseMapper = baseMapper;
        this.clazz = clazz;
    }

    @Override
    public void add(T record) {
        baseMapper.insert(record);
    }

    @Override
    public void delete(Object[] ids) {
        Example example = new Example(clazz);
        Example.Criteria criteria = example.createCriteria();

        //获取字段
        Field[] declaredFields = clazz.getDeclaredFields();

        String id="id";//主键属性的名称先使用ID
        for (Field declaredField : declaredFields) {

            if(declaredField.isAnnotationPresent(Id.class)){//注解存在
                id = declaredField.getName();
                break;
            }
        }
        criteria.andIn(id, Arrays.asList(ids));
        baseMapper.deleteByExample(example);
    }

    @Override
    public T findOne(Object id) {
        System.out.println(id);
        return baseMapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(T record) {
        updateByPrimaryKey(record);
    }


    @Override
    public int insert(T record) {
        return baseMapper.insert(record);
    }

    @Override
    public int insertSelective(T record) {
        return baseMapper.insert(record);
    }

    @Override
    public int delete(T record) {
        return baseMapper.delete(record);
    }

    @Override
    public int deleteByPrimaryKey(Object key) {
        return baseMapper.deleteByPrimaryKey(key);
    }

    @Override
    public int deleteByExample(Object example) {
        return baseMapper.deleteByExample(example);
    }

    @Override
    public T selectOne(T record) {
        return baseMapper.selectOne(record);
    }

    @Override
    public List<T> select(T record) {
        return baseMapper.select(record);
    }

    @Override
    public List<T> selectAll() {
        return baseMapper.selectAll();
    }

    @Override
    public T selectByPrimaryKey(Object key) {
        return baseMapper.selectByPrimaryKey(key);
    }

    @Override
    public List<T> selectByExample(Object example) {
        return baseMapper.selectByExample(example);
    }

    @Override
    public int updateByPrimaryKey(T record) {
        return baseMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateByPrimaryKeySelective(T record) {
        return baseMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByExample(T record, Object example) {
        return baseMapper.updateByExample(record, example);
    }

    @Override
    public int updateByExampleSelective(T record, Object example) {
        return baseMapper.updateByExampleSelective(record, example);
    }
}
