package com.common.business.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.common.core.entity.BaseEntity;

import cn.hutool.core.collection.CollUtil;

public class WjQuery{
    private WjQuery() {
    	
    }
	
	/**
     * 获取远程查询条件Build，调用list方法结束获取结果
     * @see com.common.business.wrapper.WjBuilder#list()
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T extends BaseEntity<T>> WjBuilder create(Class<T> clazz){
    	return WjBuilder.create(clazz);
    }
    
    
    /**
     * 远程实体查询
     * @param <T>
     * @param clazz
     * @param wjBuilder
     * @return
     */
    public static <T extends BaseEntity<T>> List<T> list(WjBuilder wjBuilder){
    	return wjBuilder.list();
    }
    
    /**
     * 不带条件查询所有数据
     * @see com.common.business.wrapper.WjBuilder#list()
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T extends BaseEntity<T>> List<T> list(Class<T> clazz){
    	return create(clazz).list();
    }
    
    /**
     * 通过id获取entity对象
     * @param <T>
     * @param clazz
     * @param id
     * @return
     */
    public static <T extends BaseEntity<T>> T getById(Class<T> clazz , String id) {
    	List<T> list = create(clazz).eq(BaseEntity::getId, id).list();
    	if(CollUtil.isEmpty(list)) {
    		return null;
    	}
    	return list.get(0);
    }
    
    /**
     * 通过id集合获取entity对象集合
     * @param <T>
     * @param clazz
     * @param ids
     * @return
     */
    public static <T extends BaseEntity<T>> List<T> getByIds(Class<T> clazz , Object... ids) {
    	List<T> list = create(clazz).in(BaseEntity::getId, ids).list();
    	if(list == null) {
    		list = new ArrayList<>();
    	}
    	return list;
    }
    
}
