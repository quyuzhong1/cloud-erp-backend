package com.erp.sdk.third.kingdee.utils;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.kingdee.bos.webapi.sdk.K3CloudApi;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class K3CloudApiPool  {
	private K3CloudApiPool(){
		
	}
	private static volatile GenericObjectPool<K3CloudApi> genericObjectPool = null;
	
	
	static K3CloudApi getK3CloudApi() {
		if(genericObjectPool == null) {
			synchronized (K3CloudApiPool.class) {
				if(genericObjectPool == null) {
					GenericObjectPoolConfig<K3CloudApi> objectPoolConfig = new GenericObjectPoolConfig<>();
			        objectPoolConfig.setMaxTotal(KingdeeApiUtils.MAX_CLIENT_TOTAL);
			        genericObjectPool = new GenericObjectPool<>(new K3CloudApiFactory(), objectPoolConfig);
				}
			}
		}
		
        try {
			return genericObjectPool.borrowObject();
		} catch (Exception e) {
			log.error("获取金蝶连接池失败：{}"  , e);
		}
        return null;
	}
	
	static void returnK3CloudApi(K3CloudApi k3CloudApi) {
		if(k3CloudApi == null) {
			return;
		}
		try {
			genericObjectPool.returnObject(k3CloudApi);
		} catch (Exception e) {
			log.error("返回金蝶连接池失败：{}" , k3CloudApi , e);
		}
	}
}
