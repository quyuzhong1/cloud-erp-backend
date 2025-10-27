package com.erp.server.wms.service;
import com.common.business.dto.ApproveDTO;
import com.common.business.enums.ClientTypeEnum;
import com.erp.model.wms.dto.excel.SampleBackInfoImportExcelDTO;
import com.erp.model.wms.entity.SampleBackInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleBackInfoDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 样品退回单 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleBackInfoService extends SuperService<SampleBackInfoEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleBackInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleBackInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuhaotian
    * @date: 2025-08-21
    * @param pagingParamDTO
    * @return PagingVO<SampleBackInfoDTO.ListDTO>>
    */
    PagingVO<SampleBackInfoDTO.ListDTO> paging(PagingDTO<SampleBackInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return List<SampleBackInfoDTO.TabListDTO>>
    */
    List<SampleBackInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    SampleBackInfoDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleBackInfoDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleBackInfoDTO.UpdateDTO dto);

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
    void exportList(SampleBackInfoDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleBackInfoEntity entity);

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
    * @return
    */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入样品退回单
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     */
    void importSampleBackInfo(BaseDTO.ImportDTO dto);

    /**
     * 获取样品退回单分页数据（用于异步导出）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return
     */
    PagingVO<SampleBackInfoDTO.ListDTO> getSampleBackInfoPageData(PagingDTO<SampleBackInfoDTO.ExportDTO> dto);

    /**
     * 处理导入成功的数据列表
     * @author wuhaotian
     * @date: 2025-08-21
     * @param successList 成功的数据列表
     * @param errorNoList 错误的序号列表
     * @param errorList2 错误数据列表2
     * @param importType 导入类型
     */
    void handleImportSuccessList(List<SampleBackInfoImportExcelDTO> successList, List<String> errorNoList, List<SampleBackInfoImportExcelDTO> errorList2, String importType);

    // ========== APP端专用方法 ==========

    /**
     * APP端标签页列表
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto 权限参数
     * @return 标签页列表
     */
    List<SampleBackInfoDTO.TabListDTO> tabListApp(PermissionsDTO dto);

    /**
     * APP端分页查询
     * @author wuhaotian
     * @date: 2025-09-15
     * @param pagingParamDTO 分页参数
     * @return 分页结果
     */
    PagingVO<SampleBackInfoDTO.ListDTO> pagingApp(PagingDTO<SampleBackInfoDTO.PagingParamDTO> pagingParamDTO);


    /**
     * 提交审核（带客户端类型）
     * @author wuhaotian
     * @date: 2025-09-15
     * @param id 样品退回单ID
     * @param clientType 客户端类型
     * @return 操作结果
     */
    BatchResultDTO submit(String id, ClientTypeEnum clientType);

    /**
     * 审核（带客户端类型）
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto 审核参数
     * @param clientType 客户端类型
     * @return 操作结果
     */
    BatchResultDTO approve(ApproveOneDTO dto, ClientTypeEnum clientType);

    /**
     * 反审核（带客户端类型）
     * @author wuhaotian
     * @date: 2025-09-15
     * @param id 样品退回单ID
     * @param clientType 客户端类型
     * @return 操作结果
     */
    BatchResultDTO disApprove(String id, ClientTypeEnum clientType);

    /**
     * 删除（带客户端类型）
     * @author wuhaotian
     * @date: 2025-09-15
     * @param id 样品退回单ID
     * @param clientType 客户端类型
     * @return 操作结果
     */
    BatchResultDTO delete(String id, ClientTypeEnum clientType);

    /**
     * 作废（带客户端类型）
     * @author wuhaotian
     * @date: 2025-09-15
     * @param id 样品退回单ID
     * @param remark 作废原因
     * @param clientType 客户端类型
     * @return 操作结果
     */
    BatchResultDTO invalid(String id, String remark, ClientTypeEnum clientType);

    /**
     * 撤销（带客户端类型）
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto 样品退回单ID
     * @param clientType 客户端类型
     * @return 操作结果
     */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto, ClientTypeEnum clientType);

}
