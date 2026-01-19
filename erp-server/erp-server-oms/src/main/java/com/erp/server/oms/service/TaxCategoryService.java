package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.entity.TaxCategoryEntity;

/**
 * 税种基础数据服务接口
 *
 * @author system
 * @date 2025/01/XX
 */
public interface TaxCategoryService extends SuperService<TaxCategoryEntity> {

    /**
     * 根据税种ID查询
     *
     * @param categoryId 税种ID
     * @return 税种实体
     */
    TaxCategoryEntity getByCategoryId(String categoryId);
}
