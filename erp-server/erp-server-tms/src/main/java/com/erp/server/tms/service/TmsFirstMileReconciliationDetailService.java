package com.erp.server.tms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    TmsFirstMileReconciliationDetailDTO.ImportDTO importFile(TmsFirstMileReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response);

    /**
     * 待对账分页
     *
     * @author Jim
     * @date: 2024-03-25
     */
    PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> waitReconciliationPaging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> dto);


    /**
     * 导出
     *
     * @author Jim
     * @date: 2024-03-25
     */
    void exportList(TmsFirstMileReconciliationDetailDTO.ExportDTO dto, HttpServletResponse response);


    /**
     * 通过
     *
     * @author Jim
     * @date: 2024-03-25
     */
    List<TmsFirstMileReconciliationDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 更新
     *
     * @author Jim
     * @date: 2024-03-25
     */
    Boolean update(List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailList, String mainId);

    /**
     * 补充明细信息
     *
     * @author Jim
     * @date: 2024-03-25
     */
    void fillDetailList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> viewDTOList, String currency, String currencySymbol);
}
