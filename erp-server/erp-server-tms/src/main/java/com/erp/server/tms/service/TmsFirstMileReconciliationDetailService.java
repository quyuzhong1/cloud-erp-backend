package com.erp.server.tms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 头程对账单明细 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
public interface TmsFirstMileReconciliationDetailService extends SuperService<TmsFirstMileReconciliationDetailEntity> {

    /**
     * 新增
     *
     * @param dto DTO
     * @return AddDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDetailDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto DTO
     * @return Boolean
     * @author Jim
     * @date: 2024-03-25
     */
    Boolean update(TmsFirstMileReconciliationDetailDTO.UpdateDTO dto);

    /**
     * 分页
     *
     * @param dto DTO
     * @return Boolean
     * @author Jim
     * @date: 2024-03-25
     */
    PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> paging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> dto);

    /**
     * 更新状态
     *
     * @return BatchResultDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BatchResultDTO updateStatus(String id, String status);

    /**
     * 导入
     *
     * @return ImportDTO
     * @author Jim
     * @date: 2024-03-25
     */
    TmsFirstMileReconciliationDetailDTO.ImportDTO importFile(TmsB2cDeclareReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response);

    /**
     * 待对账分页
     *
     * @return ImportDTO
     * @author Jim
     * @date: 2024-03-25
     */
    PagingVO<TmsFirstMileReconciliationDetailDTO.WaitListDTO> waitPaging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> dto);
}
