package com.common.business.threadlocal;

import com.common.business.enums.DynamicDataSourceTypeEnum;

public class DynamicDataSourceThreadLocal {
    
    private static ThreadLocal<DynamicDataSourceTypeEnum> dynamicDataSourceThread = new ThreadLocal<>();
    
    public static void set(DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum){
    	dynamicDataSourceThread.set(dynamicDataSourceTypeEnum);
    }
    
    public static DynamicDataSourceTypeEnum get(){
        return dynamicDataSourceThread.get();
    }
    
    //防止内存泄漏
    public static void remove(){
    	dynamicDataSourceThread.remove();
    }
    
    
    
}