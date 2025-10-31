package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.entity.QcNoticeEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 质检通知单 服务类
 * </p>
 *
 * @author jack
 * @since 2025-04-21
 */
public interface QcNoticeService extends SuperService<QcNoticeEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(QcNoticeDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return
    */
    Boolean update(QcNoticeDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-04-21
    * @param pagingParamDTO
    * @return PagingVO<QcNoticeDTO.ListDTO>>
    */
    PagingVO<QcNoticeDTO.ListDTO> paging(PagingDTO<QcNoticeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return List<QcNoticeDTO.TabListDTO>>
    */
    List<QcNoticeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2025-04-21
    * @param id
    * @return
    */
    QcNoticeDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(QcNoticeDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return
    */
    void updateAndSubmit(QcNoticeDTO.UpdateDTO dto);

    PagingVO<QcNoticeDTO.ListDTO> exportList(PagingDTO<QcNoticeDTO.ExportDTO> pagingParamDTO);

    /**
     * 提交审核
     * @author jack
     * @date: 2025-04-21
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author jack
    * @date: 2025-04-21
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author jack
    * @date: 2025-04-21
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return
    */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @param response
    * @return
    */
    void exportList(QcNoticeDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, QcNoticeEntity entity);

    List<QcNoticeDTO.QcInfoView> generateQcInfoView(List<String> ids);

    void generateQcInfo(List<QcNoticeDTO.QcInfoView> dto);

    int getHoursDiff(LocalDateTime approveTime, LocalDateTime nowTime);

    List<BatchResultDTO> cancelQcInfoFinish(List<String> detailIdList);

    List<BatchResultDTO>  checkInventory(QcNoticeDTO.AddDTO dto);

    QcNoticeDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response);
}
