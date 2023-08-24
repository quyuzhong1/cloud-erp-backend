package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.entity.SoB2cRefCategoryEntity;

import java.util.List;


/**
 * <p>
 * B2C销售订单分类表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cRefCategoryService extends SuperService<SoB2cRefCategoryEntity> {

    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/8/22 14:24
     * @param mainIds
     * @return List<SoB2cRefCategoryEntity>
     */
    List<SoB2cRefCategoryEntity> listByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/8/22 14:32
     * @param mainIds
     * @return Boolean
     */
    Boolean deleteByMainIds(List<String> mainIds);
}
