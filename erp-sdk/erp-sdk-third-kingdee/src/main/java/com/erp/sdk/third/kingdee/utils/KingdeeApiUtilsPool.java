package com.erp.sdk.third.kingdee.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KingdeeApiUtilsPool  {
	private KingdeeApiUtilsPool(){
		
	}
	private static Map<String, GenericObjectPool<KingdeeApiUtils>> kingdeePoolMap = new HashMap<>();
	
	private static final ReentrantLock lock = new ReentrantLock();
	
	public static KingdeeApiUtils getKingdeeApiUtils(String formId) {
		GenericObjectPool<KingdeeApiUtils> genericObjectPool = kingdeePoolMap.get(formId);
		if(genericObjectPool == null) {
			lock.lock();
			try {
				genericObjectPool = kingdeePoolMap.get(formId);
				if(genericObjectPool == null) {
					KingdeeApiUtilsFactory kingdeeApiUtilsFactory = new KingdeeApiUtilsFactory();
					kingdeeApiUtilsFactory.setFormId(formId);
					GenericObjectPoolConfig<KingdeeApiUtils> objectPoolConfig = new GenericObjectPoolConfig<>();
			        objectPoolConfig.setMaxTotal(100);
			        objectPoolConfig.setTestOnBorrow(true);
			        genericObjectPool = new GenericObjectPool<>(kingdeeApiUtilsFactory, objectPoolConfig);
			        kingdeePoolMap.put(formId, genericObjectPool);
				}
			}catch(Exception e) {
				log.error("创建金蝶连接池失败：{}" , formId , e);
			}finally {
				lock.unlock();
			}
		}
		
        try {
			return genericObjectPool.borrowObject();
		} catch (Exception e) {
			log.error("获取金蝶连接池失败：{}" , formId , e);
		}
        return null;
	}
	
	public static void returnKingdeeApiUtils(KingdeeApiUtils kingdeeApiUtils) {
		if(kingdeeApiUtils == null) {
			return;
		}
		try {
			GenericObjectPool<KingdeeApiUtils> genericObjectPool = kingdeePoolMap.get(kingdeeApiUtils.getFormId());
			genericObjectPool.returnObject(kingdeeApiUtils);
		} catch (Exception e) {
			log.error("返回金蝶连接池失败：{}" , kingdeeApiUtils , e);
		}
	}
}
