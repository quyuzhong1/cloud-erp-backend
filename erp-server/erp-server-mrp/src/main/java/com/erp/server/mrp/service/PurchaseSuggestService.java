package com.erp.server.mrp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 建议采购 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
public interface PurchaseSuggestService extends SuperService<PurchaseSuggestEntity> {

    /**
     * 查询需要生成采购建议合并的数据
     * @author will
     * @date 2025/1/6 16:23
     * @param skuIdList
     * @return List<PurchaseSuggestEntity>
     */
    List<PurchaseSuggestEntity> listGeneratePurchaseSuggestMerge(String platform, List<String> skuIdList, List<LocalDate> suggestPurchaseDateList);
}
