package com.erp.server.wms.service;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 发货计划 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface WmsDeliveryPlanService extends SuperService<WmsDeliveryPlanEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WmsDeliveryPlanDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(WmsDeliveryPlanDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author Luo_WG
    * @date: 2023-11-16
    * @param pagingParamDTO
    * @return PagingVO<OverseasDeliveryPlanDTO.ListDTO>>
    */
    PagingVO<WmsDeliveryPlanDTO.ListDTO> paging(PagingDTO<WmsDeliveryPlanDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return List<OverseasDeliveryPlanDTO.TabListDTO>>
    */
    List<WmsDeliveryPlanDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author Luo_WG
    * @date: 2023-11-16
    * @param id
    * @return
    */
    WmsDeliveryPlanDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(WmsDeliveryPlanDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    void updateAndSubmit(WmsDeliveryPlanDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author Luo_WG
     * @date: 2023-11-16
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author Luo_WG
    * @date: 2023-11-16
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author Luo_WG
    * @date: 2023-11-16
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author Luo_WG
    * @date: 2023-11-16
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author Luo_WG
    * @date: 2023-11-16
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
     * 导出Excel
     *
     * @param dto
     * @return
     * @author Luo_WG
     * @date: 2023-11-16
     */
    void exportList(WmsDeliveryPlanDTO.PagingParamDTO dto);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, WmsDeliveryPlanEntity entity);

    /**
     * 查询发货记录
     * @Author Luo_WG
     * @Date 2023/11/16 17:24
     * @param id
     * @return java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.DeliverRecordDTO>
     **/
    List<FirstMileDeliveryDTO.DeliverRecordView> listDeliverRecord(String id);

    /**
     * 下推要货申请列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 17:55
     * @param detailIds
     * @return java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>
     **/
    List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(List<String> detailIds);

    /**
     * 下推要货申请保存
     * @Author Luo_WG
     * @Date 2023/11/16 17:59
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateRequisitionApplicationSave(List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list);

    /**
     * 发货计划下推要货申请保存并提交
     * @Author Luo_WG
     * @Date 2023/11/20 9:52
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateRequisitionApplicationSaveAndSubmit(List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list);

    /**
     * 下推发货单列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 18:09
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateDeliverViewDTO>
     **/
    List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> generateDeliverView(List<String> ids);

    /**
     * 下推发货单保存
     * @Author Luo_WG
     * @Date 2023/11/16 18:11
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateDeliverSave(List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> list);

    /**
     * 下推发货单保存并提交
     * @Author Luo_WG
     * @Date 2023/11/20 9:51
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateDeliverSaveAndSubmit(List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> list);

    /**
     * 导入详情信息
     *
     * @param excelFile
     * @param thirdSkuNoList
     * @param shopId
     * @param response
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.OverseasDeliveryPlanDetailDTO.ImportDTO>
     * @Author Luo_WG
     * @Date 2023/11/23 14:17
     **/
    ListingInfoDTO.ImportDTO importFile(MultipartFile excelFile, List<String> thirdSkuNoList, String warehouseId, String shopId, HttpServletResponse response);

    /**
     * 修改发货状态
     * @Author Luo_WG
     * @Date 2023/11/30 12:11
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean updateDeliveryStatus(List<String> ids, String deliveryStatus);

    PagingVO<WmsDeliveryPlanDTO.ListDTO> exportOverseasDeliveryPlan(PagingDTO<WmsDeliveryPlanDTO.PagingParamDTO> dto);
    /**
     * 发货计划显示
     * @author will
     * @date 2024/10/23 14:43
     * @param id
     * @return WmsDeliveryPlanDTO.DeliverPlanViewDTO
     */
    WmsDeliveryPlanDTO.DeliverPlanViewDTO deliverPlanView(String id);
}
