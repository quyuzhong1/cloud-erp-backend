package com.erp.server.wms.service;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.OverseasDeliveryPlanDetailDTO;
import com.erp.model.wms.entity.OverseasDeliveryPlanEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasDeliveryPlanDTO;
import com.common.business.vo.PagingVO;
import com.sun.org.apache.xpath.internal.operations.Bool;
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
public interface OverseasDeliveryPlanService extends SuperService<OverseasDeliveryPlanEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasDeliveryPlanDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasDeliveryPlanDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author Luo_WG
    * @date: 2023-11-16
    * @param pagingParamDTO
    * @return PagingVO<OverseasDeliveryPlanDTO.ListDTO>>
    */
    PagingVO<OverseasDeliveryPlanDTO.ListDTO> paging(PagingDTO<OverseasDeliveryPlanDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return List<OverseasDeliveryPlanDTO.TabListDTO>>
    */
    List<OverseasDeliveryPlanDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author Luo_WG
    * @date: 2023-11-16
    * @param id
    * @return
    */
    OverseasDeliveryPlanDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(OverseasDeliveryPlanDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    void updateAndSubmit(OverseasDeliveryPlanDTO.UpdateDTO dto);

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
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @param response
    * @return
    */
    void exportList(OverseasDeliveryPlanDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, OverseasDeliveryPlanEntity entity);

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
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>
     **/
    List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(List<String> ids);

    /**
     * 下推要货申请保存
     * @Author Luo_WG
     * @Date 2023/11/16 17:59
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateRequisitionApplicationSave(List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list);

    /**
     * 发货计划下推要货申请保存并提交
     * @Author Luo_WG
     * @Date 2023/11/20 9:52
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateRequisitionApplicationSaveAndSubmit(List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> list);

    /**
     * 下推发货单列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 18:09
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateDeliverViewDTO>
     **/
    List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> generateDeliverView(List<String> ids);

    /**
     * 下推发货单保存
     * @Author Luo_WG
     * @Date 2023/11/16 18:11
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateDeliverSave(List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> list);

    /**
     * 下推发货单保存并提交
     * @Author Luo_WG
     * @Date 2023/11/20 9:51
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateDeliverSaveAndSubmit(List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> list);

    /**
     * 导入详情信息
     * @Author Luo_WG
     * @Date 2023/11/23 14:17
     * @param excelFile
     * @param skuIds
     * @param response
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.OverseasDeliveryPlanDetailDTO.ImportDTO>
     **/
    OverseasDeliveryPlanDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds, HttpServletResponse response);

    /**
     * 修改发货状态
     * @Author Luo_WG
     * @Date 2023/11/30 12:11
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean updateDeliveryStatus(List<String> ids, String deliveryStatus);
}
