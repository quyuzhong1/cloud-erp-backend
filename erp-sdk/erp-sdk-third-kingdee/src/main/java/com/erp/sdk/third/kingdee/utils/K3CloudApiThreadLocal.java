package com.erp.sdk.third.kingdee.utils;

import com.kingdee.bos.webapi.sdk.K3CloudApi;

public class K3CloudApiThreadLocal {
    private K3CloudApiThreadLocal() {
    	
    }
    private static final ThreadLocal<K3CloudApi> threadLocal = new ThreadLocal<>();

    public static boolean set() {
    	K3CloudApi k3CloudApi = threadLocal.get();
    	if(k3CloudApi == null) {
    		threadLocal.set(K3CloudApiPool.getK3CloudApi());
    		return false;
    	}else {
    		return true;
    	}
    }
    
    public static K3CloudApi get() {
    	K3CloudApi k3CloudApi = threadLocal.get();
    	if(k3CloudApi == null) {
    		k3CloudApi = new K3CloudApiFactory().create();
    	}
		return k3CloudApi;
    }

    public static void remove() {
    	K3CloudApiPool.returnK3CloudApi(threadLocal.get());
    	threadLocal.remove();
    }
}