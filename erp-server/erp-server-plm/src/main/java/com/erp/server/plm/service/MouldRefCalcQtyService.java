package com.erp.server.plm.service;

import com.erp.model.plm.entity.MouldRefCalcQtyEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 模具返还数量计算 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-10
 */
public interface MouldRefCalcQtyService extends SuperService<MouldRefCalcQtyEntity> {

    /**
     * 根据模具明细id查询计算数据
     * @param detailIds 明细id
     */
    List<MouldRefCalcQtyEntity> listByMouldDetailIdList(List<String> detailIds);


    MouldRefCalcQtyEntity getByDetailId(String mouldDetailId);


    void calcRefundQty();

}
