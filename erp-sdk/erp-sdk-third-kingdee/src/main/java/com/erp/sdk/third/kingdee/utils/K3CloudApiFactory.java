package com.erp.sdk.third.kingdee.utils;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kingdee.bos.webapi.entity.IdentifyInfo;
import com.kingdee.bos.webapi.sdk.K3CloudApi;

public class K3CloudApiFactory extends BasePooledObjectFactory<K3CloudApi> {

    private static final Logger logger = LoggerFactory.getLogger(K3CloudApiFactory.class);

	/**
     * 在对象池中创建对象
     *
     * @return
     * @throws Exception
     */
    @Override
    public K3CloudApi create(){
    	IdentifyInfo identifyInfo = new IdentifyInfo();
        identifyInfo.setdCID(KingdeeApiUtils.DCID);
        identifyInfo.setAppId(KingdeeApiUtils.APPID);
        identifyInfo.setUserName(KingdeeApiUtils.USERNAME);
        identifyInfo.setServerUrl(KingdeeApiUtils.SERVERURL);
        identifyInfo.setAppSecret(KingdeeApiUtils.APPSECRET);
    	K3CloudApi k3CloudApi = new K3CloudApi(identifyInfo);
    	logger.info("对象工厂创建成功：{}" , k3CloudApi);
		return k3CloudApi;
    }

    /**
     * common-pool2 中创建了 DefaultPooledObject 对象对对象池中对象进行的包装。
     * 将我们自定义的对象放置到这个包装中，工具会统计对象的状态、创建时间、更新时间、返回时间、出借时间、使用时间等等信息进行统计
     *
     * @param K3CloudApi
     * @return
     */
    @Override
    public PooledObject<K3CloudApi> wrap(K3CloudApi k3CloudApi) {
        return new DefaultPooledObject<>(k3CloudApi);
    }

}

