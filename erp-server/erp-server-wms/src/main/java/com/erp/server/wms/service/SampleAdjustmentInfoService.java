package com.erp.server.wms.service;
import com.common.business.dto.ApproveDTO;
import com.erp.model.wms.entity.SampleAdjustmentInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleAdjustmentInfoDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 样品调整单 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
 */
public interface SampleAdjustmentInfoService extends SuperService<SampleAdjustmentInfoEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleAdjustmentInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return
    */
    Boolean update(SampleAdjustmentInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuhaotian
    * @date: 2025-11-14
    * @param pagingParamDTO
    * @return PagingVO<SampleAdjustmentInfoDTO.ListDTO>>
    */
    PagingVO<SampleAdjustmentInfoDTO.ListDTO> paging(PagingDTO<SampleAdjustmentInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return List<SampleAdjustmentInfoDTO.TabListDTO>>
    */
    List<SampleAdjustmentInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-11-14
    * @param id
    * @return
    */
    SampleAdjustmentInfoDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleAdjustmentInfoDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleAdjustmentInfoDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author wuhaotian
     * @date: 2025-11-14
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author wuhaotian
    * @date: 2025-11-14
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author wuhaotian
    * @date: 2025-11-14
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author wuhaotian
    * @date: 2025-11-14
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @return
    */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author wuhaotian
    * @date: 2025-11-14
    * @param dto
    * @param response
    * @return
    */
    void exportList(SampleAdjustmentInfoDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleAdjustmentInfoEntity entity);

    /**
     * 导入Excel
     * @author wuhaotian
     * @date: 2025-11-14
     * @param dto
     * @return
     */
    Boolean importFile(BaseDTO.ImportDTO dto);

    /**
     * 异步导入样品调整单
     * @author wuhaotian
     * @date: 2025-11-14
     * @param dto
     */
    void importSampleAdjustment(BaseDTO.ImportDTO dto);

    /**
     * 处理导入成功列表
     * @author wuhaotian
     * @date: 2025-11-14
     * @param successList
     * @param errorNoList
     * @param errorList2
     * @param importType
     */
    void handleImportSuccessList(List<com.erp.model.wms.dto.excel.SampleAdjustmentImportExcelDTO> successList, List<String> errorNoList, List<com.erp.model.wms.dto.excel.SampleAdjustmentImportExcelDTO> errorList2, String importType);

    /**
     * 下载模板
     * @author wuhaotian
     * @date: 2025-11-14
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * APP端标签页列表
     * @author wuhaotian
     * @date: 2025-11-14
     * @param dto
     * @return List<SampleAdjustmentInfoDTO.TabListDTO>
     */
    List<SampleAdjustmentInfoDTO.TabListDTO> tabListApp(PermissionsDTO dto);

    /**
     * APP端分页列表查询
     * @author wuhaotian
     * @date: 2025-11-14
     * @param pagingParamDTO
     * @return PagingVO<SampleAdjustmentInfoDTO.ListDTO>
     */
    PagingVO<SampleAdjustmentInfoDTO.ListDTO> pagingApp(PagingDTO<SampleAdjustmentInfoDTO.PagingParamDTO> pagingParamDTO);

}
