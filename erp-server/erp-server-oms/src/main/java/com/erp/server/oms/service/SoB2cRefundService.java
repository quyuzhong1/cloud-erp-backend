package com.erp.server.oms.service;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cRefundDTO;
import com.erp.model.oms.entity.SoB2cRefundDetailEntity;
import com.erp.model.oms.entity.SoB2cRefundEntity;

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

    BaseResultDTO.AddDTO add(SoB2cRefundEntity soB2cRefundEntity, List<SoB2cRefundDetailEntity> soB2cRefundDetailEntityList);

    SoB2cRefundEntity getByPlatformRefundCode(String platformRefundNo);

    /**
     * 提交
     * @author will
     * @date 2025/10/24 11:47
     * @param entity
     * @param isNeedProcess
     * @return BatchResultDTO
     */
    BatchResultDTO submit(SoB2cRefundEntity entity, Boolean isNeedProcess);

    /**
     * 审核
     * @author will
     * @date 2025/10/24 11:48
     * @param dto
     * @return BatchResultDTO
     */
    BatchResultDTO approve(SoB2cRefundEntity entity,ApproveOneDTO dto);

    /**
     * 反审核
     * @author will
     * @date 2025/10/24 11:48
     * @param entity
     * @return BatchResultDTO
     */
    BatchResultDTO disApprove(SoB2cRefundEntity entity);

    /**
     * 取消流程
     * @author will
     * @date 2025/10/24 11:49
     * @param entity
     * @return BatchResultDTO
     */
    BatchResultDTO cancelProcess(SoB2cRefundEntity entity);

    /**
     * 审核完成
     * @author will
     * @date 2025/10/24 11:49
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, SoB2cRefundEntity entity);

    /**
     * 自动新增审核
     * @author will
     * @date 2025/10/24 14:15
     * @param soB2cRefundEntity
     * @param soB2cRefundDetailEntityList
     * @return void
     */
    void autoAddApprove(SoB2cRefundEntity soB2cRefundEntity, List<SoB2cRefundDetailEntity> soB2cRefundDetailEntityList);
}
