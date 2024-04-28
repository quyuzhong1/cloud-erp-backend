package com.common.business.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.common.core.entity.BaseEntity;

import cn.hutool.core.collection.CollUtil;

public class WjQuery{
    /**
     * 获取远程查询条件Build，调用list方法结束获取结果
     * @see com.common.business.wrapper.WjBuilder#list()
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T extends BaseEntity<T>> WjBuilder build(Class<T> clazz){
    	return new WjBuilder(clazz);
    }
    
    /**
     * 获取远程查询条件Build，调用list方法结束获取结果
     * @see com.common.business.wrapper.WjBuilder#list()
     * @return
     */
    public static WjBuilder build(){
    	return new WjBuilder();
    }
    
    public static <T extends BaseEntity<T>> List<T> list(Class<T> clazz , WjBuilder wjBuilder){
    	return wjBuilder.list(clazz);
    }
    
    /**
     * 不带条件查询所有数据
     * @see com.common.business.wrapper.WjBuilder#list()
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T extends BaseEntity<T>> List<T> list(Class<T> clazz){
    	return new WjBuilder().list(clazz);
    }
    
    /**
     * 通过id获取entity对象
     * @param <T>
     * @param clazz
     * @param id
     * @return
     */
    public static <T extends BaseEntity<T>> T getById(Class<T> clazz , String id) {
    	List<T> list = new WjBuilder().eq(BaseEntity::getId, id).list(clazz);
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
    	List<T> list = new WjBuilder().in(BaseEntity::getId, ids).list(clazz);
    	if(list == null) {
    		list = new ArrayList<>();
    	}
    	return list;
    }
    
}
