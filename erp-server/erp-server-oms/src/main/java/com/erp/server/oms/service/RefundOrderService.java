package com.erp.server.oms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.RefundOrderDTO;
import com.erp.model.oms.entity.RefundOrderDetailEntity;
import com.erp.model.oms.entity.RefundOrderEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 退款订单 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-25
 */
public interface RefundOrderService extends SuperService<RefundOrderEntity> {

    
    /**
     * 售后订单分页
     * @author yl
     * @date 2023-08-25 14:09
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RefundOrderDTO.PagingViewDTO>
     */
    PagingVO<RefundOrderDTO.PagingViewDTO> paging(PagingDTO<RefundOrderDTO.PagingParamDTO> dto);

    void exportExcel(RefundOrderDTO.PagingParamDTO dto);

    PagingVO<RefundOrderDTO.PagingViewDTO> exportRefund(PagingDTO<RefundOrderDTO.PagingParamDTO> dto);

    void add(RefundOrderEntity refundOrderEntity, List<RefundOrderDetailEntity> refundOrderDetailEntityList);

    RefundOrderEntity getByPlatformRefundCode(String platformRefundNo);
}
