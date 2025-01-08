package com.erp.server.tms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * b2c报关对账单 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
public interface TmsB2cDeclareReconciliationService extends SuperService<TmsB2cDeclareReconciliationEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsB2cDeclareReconciliationDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    Boolean update(TmsB2cDeclareReconciliationDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author will
    * @date: 2024-03-19
    * @param pagingParamDTO
    * @return PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO>>
    */
    PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO> paging(PagingDTO<TmsB2cDeclareReconciliationDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return List<TmsB2cDeclareReconciliationDTO.TabListDTO>>
    */
    List<TmsB2cDeclareReconciliationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2024-03-19
    * @param id
    * @return
    */
    TmsB2cDeclareReconciliationDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(TmsB2cDeclareReconciliationDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    void updateAndSubmit(TmsB2cDeclareReconciliationDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author will
     * @date: 2024-03-19
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);
    
    BatchResultDTO updatePayStatus(String id , String payStatus , LocalDateTime payTime);

    /**
    * 审核
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author will
    * @date: 2024-03-19
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author will
    * @date: 2024-03-19
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author will
    * @date: 2024-03-19
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
     * 导出Excel
     *
     * @param dto
     * @return
     * @author will
     * @date: 2024-03-19
     */
    void exportList(TmsB2cDeclareReconciliationDTO.ExportDTO dto);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, TmsB2cDeclareReconciliationEntity entity);
    /**
     * @description: 根据id查询
     * @author Will
     * @date: 2024/3/29 17:27
     * @param idList
     * @return List<TmsB2cDeclareReconciliationEntity>
     */
    List<TmsB2cDeclareReconciliationEntity> listEntityByIds(List<String> idList);

    PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO> exportB2cDeclareReconciliation(PagingDTO<TmsB2cDeclareReconciliationDTO.ExportDTO> dto);

}
