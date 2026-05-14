package com.erp.server.oms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.model.oms.dto.ExhibitionOrderImportExcelDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.ExhibitionOrderEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 展会订单信息 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-29
 */
public interface ExhibitionOrderService extends SuperService<ExhibitionOrderEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-08-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ExhibitionOrderDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-08-29
    * @param dto
    * @return
    */
    Boolean update(ExhibitionOrderDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-08-29
    * @param pagingParamDTO
    * @return PagingVO<ExhibitionOrderDTO.ListDTO>>
    */
    PagingVO<ExhibitionOrderDTO.ListDTO> paging(PagingDTO<ExhibitionOrderDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-08-29
    * @param dto
    * @return List<ExhibitionOrderDTO.TabListDTO>>
    */
    List<ExhibitionOrderDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2025-08-29
    * @param id
    * @return
    */
    ExhibitionOrderDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2025-08-29
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(ExhibitionOrderDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2025-08-29
    * @param dto
    * @return
    */
    void updateAndSubmit(ExhibitionOrderDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2025-08-29
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author jack
    * @date: 2025-08-29
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author jack
    * @date: 2025-08-29
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author jack
    * @date: 2025-08-29
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author jack
    * @date: 2025-08-29
    * @param dto
    * @return
    */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-08-29
    * @param dto
    * @param response
    * @return
    */
    void exportList(ExhibitionOrderDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, ExhibitionOrderEntity entity);
    /**
     * 作废
     * @author jack
     * @date: 2025-08-29
     * @param id
     * @return
     */
    BatchResultDTO invalid(String id,String remark);

    List<ExhibitionOrderDTO.FreezeQtyBySku> listFreezeQtyBySku(ExhibitionOrderDTO.SearchDTO dto);

    Boolean importFile(BaseDTO.ImportDTO dto);

    void importExhibitionOrder(BaseDTO.ImportDTO dto);

    void handleImportSuccessList(List<ExhibitionOrderImportExcelDTO> successList, List<String> errorNoList, List<ExhibitionOrderImportExcelDTO> errorList2, String importType);

    ExhibitionOrderDTO.DownstreamDTO generateDownstreamByExhibitionOrder(String exhibitionOrderId);

    List<ExhibitionOrderDTO.DownstreamListDTO> listSoOutstockByExhibitionId(String id);

    List<ExhibitionOrderDTO.DownstreamListDTO> listOtherInstockByExhibitionId(String id);

    List<ExhibitionOrderDTO.DownstreamListDTO> listSoByExhibitionId(String id);

    WorkflowTaskRecordDTO.MqResponseDTO generateSoInfoApprove(WorkflowTaskRecordDTO.MqRequestDTO dto);

    WorkflowTaskRecordDTO.MqResponseDTO autoSoInfoDisApprove(WorkflowTaskRecordDTO.MqRequestDTO dto);
}
