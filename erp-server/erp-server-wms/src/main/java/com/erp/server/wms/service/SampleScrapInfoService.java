package com.erp.server.wms.service;
import com.common.business.enums.ClientTypeEnum;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.dto.excel.SampleScrapImportExcelDTO;
import com.erp.model.wms.entity.SampleScrapInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.common.business.vo.PagingVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 样品报废单主表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
public interface SampleScrapInfoService extends SuperService<SampleScrapInfoEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleScrapInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    Boolean update(SampleScrapInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-08-20
    * @param pagingParamDTO
    * @return PagingVO<SampleScrapInfoDTO.ListDTO>>
    */
    PagingVO<SampleScrapInfoDTO.ListDTO> paging(PagingDTO<SampleScrapInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return List<SampleScrapInfoDTO.TabListDTO>>
    */
    List<SampleScrapInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    SampleScrapInfoDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleScrapInfoDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleScrapInfoDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2025-08-20
     * @param id
     * @return
     */
    BatchResultDTO submit(String id, ClientTypeEnum clientType);

    /**
    * 审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto, ClientTypeEnum clientType);

    /**
    * 反审核
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id, ClientTypeEnum clientType);

    /**
    * 删除
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO delete(String id, ClientTypeEnum clientType);

    /**
    * 撤销
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id, ClientTypeEnum clientType);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(SampleScrapInfoDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleScrapInfoEntity entity);
    /**
     * 导入Excel
     * @author jack
     * @date: 2025-08-20
     * @param excelFile
     * @param response
     * @return
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 作废
     * @author jack
     * @date: 2025-08-20
     * @param id
     * @return
     */
    BatchResultDTO invalid(String id,String remark, ClientTypeEnum clientType);

    void importSampleScrap(BaseDTO.ImportDTO dto);

    void handleImportSuccessList(List<SampleScrapImportExcelDTO> successList,List<String> errorNoList, List<SampleScrapImportExcelDTO> errorList2, String importType);

    Boolean importAsynExcel(BaseDTO.ImportDTO dto);

    List<SampleScrapInfoDTO.TabListDTO> tabListApp(PermissionsDTO dto);

    PagingVO<SampleScrapInfoDTO.ListDTO> pagingApp(PagingDTO<SampleScrapInfoDTO.PagingParamDTO> dto);
}
