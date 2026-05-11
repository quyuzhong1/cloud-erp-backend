package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleInitialLedgerDTO;
import com.erp.model.wms.entity.SampleInitialLedgerEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 样品期初台账 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleInitialLedgerService extends SuperService<SampleInitialLedgerEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleInitialLedgerDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleInitialLedgerDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuhaotian
    * @date: 2025-08-21
    * @param pagingParamDTO
    * @return PagingVO<SampleInitialLedgerDTO.ListDTO>>
    */
    PagingVO<SampleInitialLedgerDTO.ListDTO> paging(PagingDTO<SampleInitialLedgerDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return List<SampleInitialLedgerDTO.TabListDTO>>
    */
    List<SampleInitialLedgerDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    SampleInitialLedgerDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleInitialLedgerDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleInitialLedgerDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author wuhaotian
     * @date: 2025-08-21
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @param response
    * @return
    */
    void exportList(SampleInitialLedgerDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleInitialLedgerEntity entity);

    /**
    * 异步导入
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean importFile(BaseDTO.ImportDTO dto);

    /**
    * 下载导入模板
    * @author wuhaotian
    * @date: 2025-08-21
    * @param response
    */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入样品期初台账
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     */
    void importSampleInitialLedger(BaseDTO.ImportDTO dto);

    /**
     * 处理导入成功的数据列表
     * @author wuhaotian
     * @date: 2025-08-21
     * @param successList 成功的数据列表
     * @param errorNoList 错误的序号列表
     * @param errorList2 错误数据列表2
     * @param importType 导入类型
     */
    void handleImportSuccessList(List<com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO> successList, List<String> errorNoList, List<com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO> errorList2, String importType);

}
