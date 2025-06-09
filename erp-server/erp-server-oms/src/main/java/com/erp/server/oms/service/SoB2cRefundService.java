package com.erp.server.oms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cRefundDTO;
import com.erp.model.oms.entity.SoB2cRefundDetailEntity;
import com.erp.model.oms.entity.SoB2cRefundEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 退款订单 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
public interface SoB2cRefundService extends SuperService<SoB2cRefundEntity> {

    
    /**
     * 售后订单分页
     * @author yl
     * @date 2023-08-25 14:09
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RefundOrderDTO.PagingViewDTO>
     */
    PagingVO<SoB2cRefundDTO.PagingViewDTO> paging(PagingDTO<SoB2cRefundDTO.PagingParamDTO> dto);

    void exportExcel(SoB2cRefundDTO.PagingParamDTO dto);

    PagingVO<SoB2cRefundDTO.PagingViewDTO> exportRefund(PagingDTO<SoB2cRefundDTO.PagingParamDTO> dto);

    void add(SoB2cRefundEntity soB2cRefundEntity, List<SoB2cRefundDetailEntity> soB2cRefundDetailEntityList);

    SoB2cRefundEntity getByPlatformRefundCode(String platformRefundNo);
}
