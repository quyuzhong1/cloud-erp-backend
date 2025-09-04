package com.erp.sdk.third.kingdee.utils;

import java.util.Date;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.date.DateUtil;

public class KingdeeApiUtilsFactory extends BasePooledObjectFactory<KingdeeApiUtils> {

    private static final Logger logger = LoggerFactory.getLogger(KingdeeApiUtilsFactory.class);

    private String formId;
    
    public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	/**
     * 在对象池中创建对象
     *
     * @return
     * @throws Exception
     */
    @Override
    public KingdeeApiUtils create() throws Exception {
        // 实现线程安全避免在高并发的场景下出现clientId重复导致无法创建连接的情况
        KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(formId);
        kingdeeApiUtils.setCreateTime(new Date());
        return kingdeeApiUtils;
    }

    /**
     * common-pool2 中创建了 DefaultPooledObject 对象对对象池中对象进行的包装。
     * 将我们自定义的对象放置到这个包装中，工具会统计对象的状态、创建时间、更新时间、返回时间、出借时间、使用时间等等信息进行统计
     *
     * @param KingdeeApiUtils
     * @return
     */
    @Override
    public PooledObject<KingdeeApiUtils> wrap(KingdeeApiUtils kingdeeApiUtils) {
        return new DefaultPooledObject<>(kingdeeApiUtils);
    }

    /**
     * 销毁对象
     *
     * @param p 对象池
     * @throws Exception 异常
     */
    @Override
    public void destroyObject(PooledObject<KingdeeApiUtils> p) throws Exception {
        super.destroyObject(p);
    }

    /**
     * 校验对象是否可用
     *
     * @param p 对象池
     * @return 对象是否可用结果，boolean
     */
    @Override
    public boolean validateObject(PooledObject<KingdeeApiUtils> p) {
    	KingdeeApiUtils object = p.getObject();
    	if(object == null) {
    		return false;
    	}
    	Date createTime = object.getCreateTime();
    	if(createTime == null) {
    		return false;
    	}
    	if(new Date().after(DateUtil.offsetSecond(createTime, 9 * 60))) {
    		return false;
    	}
        return super.validateObject(p);
    }

    /**
     * 激活钝化的对象系列操作
     *
     * @param p 对象池
     * @throws Exception 异常信息
     */
    @Override
    public void activateObject(PooledObject<KingdeeApiUtils> p) throws Exception {
        super.activateObject(p);
    }

    /**
     * 钝化未使用的对象
     *
     * @param p 对象池
     * @throws Exception 异常信息
     */
    @Override
    public void passivateObject(PooledObject<KingdeeApiUtils> p) throws Exception {
        super.passivateObject(p);
    }
}


