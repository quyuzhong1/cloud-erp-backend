package com.erp.server.scm.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.AssetNoticeDTO;
import com.erp.model.scm.dto.AssetNoticeDetailDTO;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.excel.AssetNoticeImportExcelDTO;
import com.erp.model.scm.entity.AssetNoticeEntity;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
public interface AssetNoticeService extends SuperService<AssetNoticeEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetNoticeDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    Boolean update(AssetNoticeDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wtr
    * @date: 2025-10-16
    * @param pagingParamDTO
    * @return PagingVO<AssetNoticeDTO.ListDTO>>
    */
    PagingVO<AssetNoticeDTO.ListDTO> paging(PagingDTO<AssetNoticeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return List<AssetNoticeDTO.TabListDTO>>
    */
    List<AssetNoticeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @return
    */
    AssetNoticeDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(AssetNoticeDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    void updateAndSubmit(AssetNoticeDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author wtr
     * @date: 2025-10-16
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    List<AssetNoticeDTO.ViewGeneratePurchaseOrderDTO> viewGeneratePurchaseOrder(List<String> idList);

    Boolean generatePurchaseOrder(List<AssetNoticeDTO.ListGeneratePurchaseOrderDTO> dtoList);

    /**
    * 反审核
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @param response
    * @return
    */
    void exportList(AssetNoticeDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, AssetNoticeEntity entity);


    Boolean importFile(BaseDTO.ImportDTO dto);

    BatchResultDTO invalid(AssetNoticeEntity entity,String remark);

    void handleImportSuccessList(List<AssetNoticeImportExcelDTO> successList,
                                 List<String> errorNoList, 
                                 List<AssetNoticeImportExcelDTO> errorList2, 
                                 String importType);

    /**
     * 导入单条开模通知单（独立事务，避免同批次部分失败导致 PG 事务 aborted）
     */
    void saveImportSerialNumber(AssetNoticeDetailDTO.MoldImportDTO moldImportDTO);

    /**
     *
     * @param dto
     */
    void importAssetNotice(BaseDTO.ImportDTO dto);

    /**
     * 查询 DFM 附件列表（列表/弹窗下载）
     */
    List<AttachmentDTO.UpdateDTO> listDfmAttachment(String id);

    /**
     * 上传 DFM 附件
     */
    BatchResultDTO uploadDfmAttachment(AssetNoticeDTO.UploadDfmAttachmentDTO dto);

    /**
     * 删除 DFM 附件
     */
    BatchResultDTO deleteDfmAttachment(AttachmentDTO.DeleteDTO dto);

    /**
     * 查询 DFM 附件为空、且创建时间满足条件的开模通知单（供定时任务使用）
     */
    List<AssetNoticeDTO.MissingDfmNoticeDTO> listMissingDfmAttachment(LocalDate targetCreateDate, int batchLimit);

    /**
     * DFM 附件缺失飞书提醒（XXL-JOB 调用）
     *
     * @return 成功发送通知条数
     */
    int notifyMissingDfmAttachment(AssetNoticeDTO.DfmAttachmentNoticeJobParamDTO param);
}
