package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.entity.TransferInEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 分布式调入单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferInService extends SuperService<TransferInEntity> {

    List<TransferInDTO.TabListDTO> tabList(PermissionsDTO dto);

    
    /**
     * 下推单据保存
     * @author yl
     * @date 2023-05-26 11:33
     * @param list
     * @return java.lang.Boolean
     */
    Boolean generateTransferIn(ValidList<TransferInDTO.ViewGenerateTransferInDTO> list);

    /**
     * 分页查询
     * @author yl
     * @date 2023-05-26 16:04
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.TransferInDTO.PagingViewDTO>
     */
    PagingVO<TransferInDTO.PagingViewDTO> paging(PagingDTO<TransferInDTO.PagingParamDTO> dto);

    /**
     * 提交审核
     * @author yl
     * @date 2023-05-26 16:52
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    /**
     * 审核
     * @author yl
     * @date 2023-05-26 16:58
     * @param dto
     * @return java.lang.Boolean
     */
    BatchResultDTO approve(ApproveOneDTO dto, TransferInEntity transferInEntity);

    /**
     * 审核结束
     * @author will
     * @date 2025/4/22 15:03
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, TransferInEntity entity);
    /**
     * 撤销流程
     * @author yl
     * @date 2023-05-26 19:00
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto);

    /**
     * 删除分布是调入单
     * @author yl
     * @date 2023-05-26 19:05
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    /**
     * 原子批量删除分布式调入单
     * @author yl
     * @date 2023-05-26 19:05
     * @param ids
     * @param returnDetails
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails);

    /**
     * 删除单个分布式调入单
     * @author yl
     * @date 2023-05-26 19:05
     * @param entity
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO deleteEntity(TransferInEntity entity);

    /**
     * 根据ID列表获取实体Map
     * @author yl
     * @date 2023-05-26 19:05
     * @param ids
     * @return java.util.Map<java.lang.String, com.erp.model.wms.entity.TransferInEntity>
     */
    Map<String, TransferInEntity> mapByIds(List<String> ids);

    /**
     * 反审核
     * @author yl
     * @date 2023-05-29 9:47
     * @param dto
     * @return java.lang.Boolean
     */
    BatchResultDTO disApprove(TransferInEntity entity);

    /**
     * 作废
     * @author yl
     * @date 2023-05-29 9:51
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     */
    Boolean invalid(List<String> ids, String remark);

    
    /**
     * 导出数据
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-29 10:04
     */
    Boolean exportExcel(TransferInDTO.ExportDTO dto);

    /**
     * 分布是调入详情
     * @author yl
     * @date 2023-05-29 10:52
     * @param id
     * @return com.erp.model.wms.dto.TransferInDTO.ViewDTO
     */
    TransferInDTO.ViewDTO view(String id);

    
    /**
     * 更改分布式调入单
     * @author yl
     * @date 2023-05-29 14:02
     * @param dto
     * @return java.lang.String
     */
    String updateTransferIn(TransferInDTO.UpdateDTO dto);

    
    /**
     * 修改并提交
     * @author yl
     * @date 2023-05-29 14:27
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(TransferInDTO.UpdateDTO dto);

    /**
     * 根据来源id集合查询分步式调入单
     * @param sourceIds
     * @return
     */
    List<TransferInEntity> listBySourceIds(List<String> sourceIds);

    PagingVO<TransferInDTO.PagingViewDTO> exportTransferIn(PagingDTO<TransferInDTO.ExportDTO> dto);
    /**
     * 更新金蝶id
     * @author will
     * @date 2025/4/23 18:29
     * @param businessId
     * @param syncKingdeeId
     * @return void
     */
    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId);
}
