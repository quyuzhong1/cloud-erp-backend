package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.InitStockDetailEntity;

import java.util.List;

/**
 * @Classname: InitStockDetailService
 * @Description: TODO
 * @CreateTime: 2023-05-11  10:31
 * @Author: zhangchunlin
 */
public interface InitStockDetailService  extends SuperService<InitStockDetailEntity> {

    /**
     * 根据主单id获取明细数据
     * @param mainId
     * @return
     */
    List<InitStockDetailEntity> findList(String mainId);

}
