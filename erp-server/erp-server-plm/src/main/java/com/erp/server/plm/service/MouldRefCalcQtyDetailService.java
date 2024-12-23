package com.erp.server.plm.service;

import com.erp.model.plm.entity.MouldRefCalcQtyDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 计算量明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-10
 */
public interface MouldRefCalcQtyDetailService extends SuperService<MouldRefCalcQtyDetailEntity> {

    /**
     * 根据主表id查明细
     * @param id id
     */
    List<MouldRefCalcQtyDetailEntity> listByMainId(String id);

    /**
     * 根据主表id查明细
     * @param mouldRefMouldRefIds id
     */
    List<MouldRefCalcQtyDetailEntity> listByMainIds(List<String> mouldRefMouldRefIds);
}
