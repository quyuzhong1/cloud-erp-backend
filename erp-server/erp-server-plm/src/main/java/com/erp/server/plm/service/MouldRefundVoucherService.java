package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.dto.MouldRefundVoucherDTO;
import com.erp.model.plm.entity.MouldRefundVoucherEntity;

/**
 * <p>
 * 费用返还详情(凭据) 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
public interface MouldRefundVoucherService extends SuperService<MouldRefundVoucherEntity> {

    /**
     * 费用返还确认
     * @param dto 参数
     */
    void returnConfirm(MouldInfoDTO.ReturnConfirmDTO dto);

    /**
     * 费用返还确认详情
     * @param detailId 参数
     */
    MouldRefundVoucherDTO returnConfirmDetail(String detailId);
}
