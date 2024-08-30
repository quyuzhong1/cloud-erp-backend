package com.erp.server.tms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 头程对账单 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
public interface TmsFirstMileReconciliationService extends SuperService<TmsFirstMileReconciliationEntity> {

    /**
     * 新增
     *
     * @param dto DTO
     * @return AddDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto DTO
     * @return Boolean
     * @author Jim
     * @date: 2024-03-25
     */
    Boolean update(TmsFirstMileReconciliationDTO.UpdateDTO dto);

    /**
     * 分页列表查询
     *
     * @param pagingParamDTO DTO
     * @return PagingVO<TmsFirstMileReconciliationDTO.ListDTO>>
     * @author Jim
     * @date: 2024-03-25
     */
    PagingVO<TmsFirstMileReconciliationDTO.ListDTO> paging(PagingDTO<TmsFirstMileReconciliationDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     *
     * @param dto DTO
     * @return List<TmsFirstMileReconciliationDTO.TabListDTO>>
     * @author Jim
     * @date: 2024-03-25
     */
    List<TmsFirstMileReconciliationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 详情
     *
     * @param id id
     * @return ViewDTO
     * @author Jim
     * @date: 2024-03-25
     */
    TmsFirstMileReconciliationDTO.ViewDTO view(String id);

    /**
     * 新增并提交审核
     *
     * @param dto DTO
     * @return BaseResultDTO.AddDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BaseResultDTO.AddDTO addAndSubmit(TmsFirstMileReconciliationDTO.AddDTO dto);

    /**
     * 修改并提交审核
     *
     * @param dto DTO
     * @author Jim
     * @date: 2024-03-25
     */
    void updateAndSubmit(TmsFirstMileReconciliationDTO.UpdateDTO dto);

    /**
     * 提交审核
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2024-03-25
     */
    BatchResultDTO submit(String id);

    /**
     * 审核
     *
     * @param dto DTO
     * @return BatchResultDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
     * 反审核
     *
     * @param id id
     * @return BatchResultDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BatchResultDTO disApprove(String id);

    /**
     * 删除
     *
     * @param id id
     * @return BatchResultDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BatchResultDTO delete(String id);

    /**
     * 撤销
     *
     * @param id id
     * @return BatchResultDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BatchResultDTO cancelProcess(String id);

    /**
     * 导出Excel
     *
     * @param dto DTO
     * @author Jim
     * @date: 2024-03-25
     */
    void exportList(TmsFirstMileReconciliationDTO.ExportDTO dto);

    /**
     * 审核通过回调方法
     *
     * @param dto    DTO
     * @param entity Entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, TmsFirstMileReconciliationEntity entity);

    /**
     * 根据审核状态查询单据
     */
    List<TmsFirstMileLogisticDTO.WaitSubmitListDTO> listByApproveStatus(String status);


    TmsFirstMileReconciliationEntity getByGenerate(String logisticsSupplierId, LocalDate startDate, LocalDate endDate, String currency);

    String checkAndGetSupplier(String id);

    TmsFirstMileReconciliationEntity findByCycleAndSupplier(String supplier, String currency, LocalDate startDate, LocalDate endDate);

    TmsFirstMileReconciliationEntity getByCode(String code);

    PagingVO<TmsFirstMileReconciliationDTO.ListDTO> exportFirstMileReconciliation(PagingDTO<TmsFirstMileReconciliationDTO.ExportDTO> dto);
}
