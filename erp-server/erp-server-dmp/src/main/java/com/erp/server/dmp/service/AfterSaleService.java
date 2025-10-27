package com.erp.server.dmp.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import com.erp.model.dmp.entity.AfterSaleEntity;
import com.sdk.wx.miniapp.response.WxJscodeToSessionResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 售后申请表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-04-06
 */
public interface AfterSaleService extends SuperService<AfterSaleEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AfterSaleDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return
    */
    Boolean update(AfterSaleDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-04-06
    * @param pagingParamDTO
    * @return PagingVO<AfterSaleDTO.ListDTO>>
    */
    PagingVO<AfterSaleDTO.ListDTO> paging(PagingDTO<AfterSaleDTO.PagingParamDTO> pagingParamDTO);

    PagingVO<DmpAfterSaleExcelDTO> exportList(PagingDTO<AfterSaleDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return List<AfterSaleDTO.TabListDTO>>
    */
    List<AfterSaleDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2025-04-06
    * @param id
    * @return
    */
    AfterSaleDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(AfterSaleDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return
    */
    void updateAndSubmit(AfterSaleDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2025-04-06
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author jack
    * @date: 2025-04-06
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author jack
    * @date: 2025-04-06
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author jack
    * @date: 2025-04-06
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return
    */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return
    */
    void exportList(AfterSaleDTO.PagingParamDTO dto,HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, AfterSaleEntity entity);

    AfterSaleProgressDTO.RepairRecordDTO getRepairProgress(AfterSaleDTO.ProgressDTO dto);

    List<AfterSaleDTO.NodeDTO>  getNodeList();

    List<BatchResultDTO> changeStatus(AfterSaleDTO.IdsDTO dto);

    List<AfterSaleProgressDTO.RepairHistoryListDTO> getRepairHistory(AfterSaleDTO.ThridUserDTO dto);

    String getAccessToken();

    WxJscodeToSessionResponse jsCode2SessionInfo(String jsCode);

    void syncWdtToAfterSale();

    List<AfterSaleDTO.DropDownDTO> getDetailByPlatformCode(String platformCode);

    Boolean udpateTrackNo(AfterSaleDTO.UpdateTrackNoDTO dto);

    BatchResultDTO invalidByCode(String code);
}
