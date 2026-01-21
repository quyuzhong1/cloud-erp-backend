package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.entity.TaxCategoryEntity;

import java.util.List;
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
     * @deprecated 请使用 {@link #getByCategoryIdAndCompanyId(String, String)} 方法，携带companyId参数以避免多条记录问题
     */
    @Deprecated
    TaxCategoryEntity getByCategoryId(String categoryId);

    /**
     * 根据税种ID和公司ID查询
     *
     * @param categoryId 税种ID
     * @param companyId 公司ID（可选，如果提供则精确匹配）
     * @return 税种实体
     */
    TaxCategoryEntity getByCategoryIdAndCompanyId(String categoryId, String companyId);

    /**
     * 初始化税种列表
     * 从第三方系统获取税种列表并初始化到cfg_tax_category表
     * 复用定时任务的逻辑
     * 
     * @return 初始化结果信息
     */
    Map<String, Object> initTaxCategoryList();

    /**
     * 根据公司ID查询税种列表
     * 查询指定公司关联的税种数据
     * 
     * @param companyId 公司ID（cfg_invoice_setting.company_id）
     * @return 税种列表
     */
    List<TaxCategoryEntity> getByCompanyId(String companyId);
}
