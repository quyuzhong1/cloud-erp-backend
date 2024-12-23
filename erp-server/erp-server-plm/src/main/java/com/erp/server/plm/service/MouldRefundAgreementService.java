package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.entity.MouldRefundAgreementEntity;

import java.util.List;

/**
 * <p>
 * 合同返还约定 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
public interface MouldRefundAgreementService extends SuperService<MouldRefundAgreementEntity> {


    List<MouldRefundAgreementEntity> listByMouldDetailIdList(List<String> detailIds);

    /**
     * 根据状态和模具明细id查询
     * @param detailIds 明细id
     * @param code 状态
     */
    List<String> listByMouldDetailIdListAndStatus(List<String> detailIds, String code);
}
