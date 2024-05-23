package com.common.business.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.nacos.common.utils.Objects;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;

import cn.hutool.core.collection.CollUtil;

public class FeignQuery{
    private FeignQuery() {
    	
    }
	
	/**
     * 获取远程查询条件Build，调用list方法结束获取结果
     * @see com.common.business.wrapper.FeignBuilder#list()
     * @param <T>
     * @param clazz 数据库实体类
     * @return
     */
    public static <T extends BaseEntity<T>> FeignBuilder create(Class<T> clazz){
    	return FeignBuilder.create(clazz);
    }
    
    
    /**
     * 远程实体查询
     * @param <T>
     * @param feignBuilder 数据库实体类条件build
     * @return
     */
    public static <T extends BaseEntity<T>> List<T> list(FeignBuilder feignBuilder){
    	return feignBuilder.list();
    }
    
    /**
     * 不带条件查询所有数据
     * @see com.common.business.wrapper.FeignBuilder#list()
     * @param <T>
     * @param clazz 数据库实体类
     * @return
     */
    public static <T extends BaseEntity<T>> List<T> list(Class<T> clazz){
    	return create(clazz).list();
    }
    
    /**
     * 通过id获取entity对象
     * @param <T>
     * @param clazz 数据库实体类
     * @param id 数据库实体主键
     * @return
     */
    public static <T extends BaseEntity<T>> T getById(Class<T> clazz , String id) {
    	if(id == null || "".equals(id)) {
    		return null;
    	}
    	List<T> list = create(clazz).eq(BaseEntity::getId, id).list();
    	if(CollUtil.isEmpty(list)) {
    		return null;
    	}
    	return list.get(0);
    }
    
    /**
     * 通过id集合获取entity对象集合
     * @param <T>
     * @param clazz 数据库实体类
     * @param ids 数据库实体主键集合
     * @return
     */
    public static <T extends BaseEntity<T>> List<T> getByIds(Class<T> clazz , Object... ids) {
    	if(ids == null || ids.length == 0) {
    		return new ArrayList<>();
    	}
    	
    	List<Object> idsObject = Arrays.asList(ids).stream().filter(Objects::nonNull).collect(Collectors.toList());
    	if(idsObject.get(0) instanceof List) {
    		idsObject = (List<Object>) idsObject.get(0);
    	}else if (idsObject.get(0) instanceof Set) {
    		idsObject = new ArrayList<>((Set<Object>)idsObject.get(0));
    	}
    	
    	if(CollUtil.isEmpty(idsObject)) {
    		return new ArrayList<>();
    	}
    	List<T> list = create(clazz).in(BaseEntity::getId, idsObject).list();
    	if(list == null) {
    		list = new ArrayList<>();
    	}
    	return list;
    }
    
    /**
     * 统一远程无参方法，无返回值
     * @param <T>
     * @param className 远程类全名称
     * @param methodName 远程方法名称
     * @return
     */
    public static void invoke(String className , String methodName) {
    	invoke(null, className, methodName);
    }
    
    /**
     * 统一远程有参方法，无返回值
     * @param <T>
     * @param className 远程类全名称
     * @param methodName 远程方法名称
     * @param param 远程方法参数
     * @return
     */
    public static void invoke(String className , String methodName , List<Object> param) {
    	invoke(null, className, methodName, param);
    }
    
    /**
     * 统一远程有参方法，有返回值
     * @param <T>
     * @param returnClazz 返回对象类名
     * @param className 远程类全名称
     * @param methodName 远程方法名称
     * @param param 远程方法参数
     * @return
     */
    public static <T> T invoke(Class<T> returnClazz , String className , String methodName , List<Object> param) {
    	if(className == null || "".equals(className)) {
    		throw new ServiceException("调用远程类不能为空");
    	}
    	if(methodName == null || "".equals(methodName)) {
    		throw new ServiceException("调用远程方法不能为空");
    	}
    	FeignInvoke feignInvoke = new FeignInvoke(className , methodName , param);
		return FeignBuilder.create(returnClazz).invoke(feignInvoke);
    }
    
    /**
     * 统一远程无参方法，有返回值
     * @param <T>
     * @param returnClazz 返回对象类名
     * @param className 远程类全名称
     * @param methodName 远程方法名称
     * @return
     */
    public static <T> T invoke(Class<T> returnClazz , String className , String methodName) {
    	return invoke(returnClazz , className, methodName , null);
    }
    
    /**
     * 统一远程有参方法，返回集合
     * @param <T>
     * @param returnClazz 返回对象类名
     * @param className 远程类全名称
     * @param methodName 远程方法名称
     * @param param 远程方法参数
     * @return
     */
    public static <T> List<T> invokeList(Class<T> returnClazz , String className , String methodName , List<Object> param) {
    	if(className == null || "".equals(className)) {
    		throw new ServiceException("调用远程类不能为空");
    	}
    	if(methodName == null || "".equals(methodName)) {
    		throw new ServiceException("调用远程方法不能为空");
    	}
    	FeignInvoke feignInvoke = new FeignInvoke(className , methodName , param);
		return FeignBuilder.create(returnClazz).invokeList(feignInvoke);
    }
    
    /**
     * 统一远程无参方法，返回集合
     * @param <T>
     * @param returnClazz 返回对象类名
     * @param className 远程类全名称
     * @param methodName 远程方法名称
     * @return
     */
    public static <T> List<T> invokeList(Class<T> returnClazz , String className , String methodName) {
    	return invokeList(returnClazz , className, methodName , null);
    }
    
}
