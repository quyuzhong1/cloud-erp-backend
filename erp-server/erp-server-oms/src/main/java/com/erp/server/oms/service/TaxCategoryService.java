package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.entity.TaxCategoryEntity;

import java.util.Map;

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

    /**
     * 初始化税种列表
     * 从第三方系统获取税种列表并初始化到cfg_tax_category表
     * 复用定时任务的逻辑
     * 
     * @return 初始化结果信息
     */
    Map<String, Object> initTaxCategoryList();
}
