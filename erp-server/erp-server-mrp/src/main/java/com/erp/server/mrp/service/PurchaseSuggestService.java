package com.erp.server.mrp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;

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
    * 新增
    * @author will
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PurchaseSuggestDTO.AddDTO dto);

    /**
     * 查询需要生成采购建议合并的数据
     * @author will
     * @date 2025/1/6 16:23
     * @param purchaseSuggestEntity
     * @return List<PurchaseSuggestEntity>
     */
    List<PurchaseSuggestEntity> listGeneratePurchaseSuggestMerge(PurchaseSuggestEntity purchaseSuggestEntity);
}
