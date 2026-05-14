package com.erp.server.dmp.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import com.erp.model.dmp.entity.AfterSaleEntity;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.sdk.wx.miniapp.response.WxJscodeToSessionResponse;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

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

    Map<String, String> listCsAgent(AfterSaleDTO.ListCsAgentDTO dto);

    /**
     * 物流下单
     *
     * @param dto AfterSaleDTO.LogisticsOrderDTO
     */
    List<BatchResultDTO> logisticsOrder(AfterSaleDTO.LogisticsOrderDTO dto);

    /**
     * 取消物流下单
     *
     * @param dto AfterSaleDTO.IdsDTO
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> batchCancel(AfterSaleDTO.IdsDTO dto);

    /**
     * 上传物流面单
     *
     * @param dto AfterSaleDTO.UploadFileDTO
     * @return String
     */
    String uploadLogisticLabel(AfterSaleDTO.UploadFileDTO dto);

    /**
     * 获取下单预览
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return List<AfterSaleDTO.OrderInfoDTO>
     */
    List<AfterSaleDTO.OrderInfoDTO> getPlaceOrderPreview(BaseIdsDTO.IdsDTO dto);

    /**
     * 打印物流面单预览
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return AfterSaleDTO.LogisticsLabelPreviewDTO
     */
    AfterSaleDTO.LogisticsLabelPreviewDTO printLogisticsLabelPreview(BaseIdsDTO.IdsDTO dto);

    /**
     * 打印物流面单确认
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return String
     */
    String printLogisticsLabelConfirm(BaseIdsDTO.IdsDTO dto);

    /**
     * 获取物流下单面单
     *
     * @param logisticsLabelDTOS List<LogisticsOrderDTO.LogisticsLabelDTO>
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> getLogisticsOrderLabel(List<LogisticsOrderDTO.LogisticsLabelDTO> logisticsLabelDTOS);

    /**
     * 手工批量获取物流面单
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> manualBatchGetLabel(BaseIdsDTO.IdsDTO dto);
}
