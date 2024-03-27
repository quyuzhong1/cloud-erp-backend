package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * b2c报关对账单明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
public interface TmsB2cDeclareReconciliationDetailService extends SuperService<TmsB2cDeclareReconciliationDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-19
    * @param detailList
    * @return
    */
    BaseResultDTO.AddDTO add(List<TmsB2cDeclareReconciliationDetailDTO.AddDTO> detailList);

    /**
    * 修改
    * @author will
    * @date: 2024-03-19
    * @param detailList
    * @param detailList
    * @return
    */
    Boolean update(List<TmsB2cDeclareReconciliationDetailDTO.UpdateDTO> detailList,String mianId);

    /**
     * @description: 导出明细数据
     * @author Will
     * @date: 2024/3/26 10:00
     * @param dto
     * @param response
     */
    void exportDetailList(TmsB2cDeclareReconciliationDetailDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/3/26 10:48
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO> paging(PagingDTO<TmsB2cDeclareReconciliationDetailDTO.PagingParamDTO> dto);
    /**
     * @description: 根据主表i集合查询
     * @author Will
     * @date: 2024/3/26 14:17
     * @param mainIdList
     * @return List<TmsB2cDeclareReconciliationDetailEntity>
     */
    List<TmsB2cDeclareReconciliationDetailEntity> listMainIdList (List<String> mainIdList);
    /**
     * @description: 更新确认状态
     * @author Will
     * @date: 2024/3/26 14:28
     * @param id
     * @param status
     * @return AddDTO
     */
    BatchResultDTO updateStatus(String id, String status);
    /**
     * @description: 清除
     * @author Will
     * @date: 2024/3/26 15:08
     * @param id
     * @return Boolean
     */
    Boolean cleanDetailMainId(String id);
    /**
     * @description: 导入
     * @author Will
     * @date: 2024/3/27 11:35
     * @param excelImportDTO
     * @param response
     * @return ImportDTO
     */
    TmsB2cDeclareReconciliationDetailDTO.ImportDTO importFile(TmsB2cDeclareReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response);
}
