package com.erp.server.dmp.handler;

import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;

/**
 *  Mongo业务处理
 *
 * @author Jim
 * @date 2024/1/24
 *
 */
public abstract class DmpMongoHandler {

    /**
     * 指定根据业务类型和当前条件查询对应mongo表处理
     *
     * @author Jim
     * @date 2024/1/24
     *
     */
    public abstract Integer findAndFillDataOrHandle(DmpMongoHandleTaskEntity mongoHandleTaskEntity, Boolean skipHistory);
}
