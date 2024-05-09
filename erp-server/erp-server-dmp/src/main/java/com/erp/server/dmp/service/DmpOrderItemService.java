package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpOrderItemEntity;

/**
 * <p>
 * 订单商品信息拆分前表 服务类
 * </p>
 */
public interface DmpOrderItemService extends IService<DmpOrderItemEntity> {

    void initItem();

}
