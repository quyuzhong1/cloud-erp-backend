package com.erp.server.wms.service;
import com.common.business.enums.ClientTypeEnum;
import com.erp.model.wms.entity.SampleTransferInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleTransferInfoDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 样品转移单主表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-10-28
 */
public interface SampleTransferInfoService extends SuperService<SampleTransferInfoEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleTransferInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    Boolean update(SampleTransferInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuhaotian
    * @date: 2025-10-28
    * @param pagingParamDTO
    * @return PagingVO<SampleTransferInfoDTO.ListDTO>>
    */
    PagingVO<SampleTransferInfoDTO.ListDTO> paging(PagingDTO<SampleTransferInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return List<SampleTransferInfoDTO.TabListDTO>>
    */
    List<SampleTransferInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-10-28
    * @param id
    * @return
    */
    SampleTransferInfoDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleTransferInfoDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleTransferInfoDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author wuhaotian
     * @date: 2025-10-28
     * @param id 主表ID
     * @param clientType 客户端类型
     * @return
     */
    BatchResultDTO submit(String id, ClientTypeEnum clientType);

    /**
    * 审核
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @param clientType 客户端类型
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto, ClientTypeEnum clientType);

    /**
    * 反审核
    * @author wuhaotian
    * @date: 2025-10-28
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id, ClientTypeEnum clientType);

    /**
    * 删除
    * @author wuhaotian
    * @date: 2025-10-28
    * @param id
    * @return
    */
    BatchResultDTO delete(String id, ClientTypeEnum clientType);
    /**
    * 作废
    * @author wuhaotian
    * @date: 2025-10-28
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author wuhaotian
    * @date: 2025-10-28
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @param response
    * @return
    */
    void exportList(SampleTransferInfoDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 获取样品转移单分页数据（用于异步导出）
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    PagingVO<SampleTransferInfoDTO.ListDTO> getSampleTransferInfoPageData(PagingDTO<SampleTransferInfoDTO.ExportDTO> dto);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleTransferInfoEntity entity);

    /**
    * 导入样品转移单
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    Boolean importFile(BaseDTO.ImportDTO dto);

    /**
    * 导入样品转移单处理
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    */
    void importSampleTransfer(BaseDTO.ImportDTO dto);

    /**
    * 处理导入成功的数据列表
    * @author wuhaotian
    * @date: 2025-10-28
    * @param successList
    * @param errorNoList
    * @param errorList2
    * @param importType
    */
    void handleImportSuccessList(List<com.erp.model.wms.dto.excel.SampleTransferImportExcelDTO> successList, 
                                  List<String> errorNoList, 
                                  List<com.erp.model.wms.dto.excel.SampleTransferImportExcelDTO> errorList2, 
                                  String importType);

    /**
    * 下载导入模板
    * @author wuhaotian
    * @date: 2025-10-28
    * @param response
    */
    void downloadTemplate(HttpServletResponse response);

}
