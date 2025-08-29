package com.erp.sdk.third.kingdee.utils;

public class KingdeeApiThreadLocal {
    
    private static ThreadLocal<KingdeeApiUtils> kingdeeApiThread = new ThreadLocal<>();
    
    public static void set(KingdeeApiUtils kingdeeApiUtils){
    	kingdeeApiThread.set(kingdeeApiUtils);
    }
    
    public static KingdeeApiUtils get(){
        return kingdeeApiThread.get();
    }
    
    //防止内存泄漏
    public static void remove(){
    	kingdeeApiThread.remove();
    }
    
}