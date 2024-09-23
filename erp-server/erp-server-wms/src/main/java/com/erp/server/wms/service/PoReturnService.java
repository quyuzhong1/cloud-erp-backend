package com.erp.server.wms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDetailDTO;
import com.erp.model.wms.dto.PurchaseReturnStatisticsDTO;
import com.erp.model.wms.entity.PoInstockEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>
 * 采购退货单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-07
 */
public interface PoReturnService extends SuperService<PoReturnEntity> {
    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>
     **/
    PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> paging(PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    String add(PurchaseReturnOrderDTO.AddDTO dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(PurchaseReturnOrderDTO.UpdateDTO dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewDTO
     **/
    PurchaseReturnOrderDTO.ViewDTO view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/14 10:04
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean submit(List<String> ids);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean addAndSubmit(PurchaseReturnOrderDTO.AddDTO dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(PurchaseReturnOrderDTO.UpdateDTO dto);

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     **/
    BatchResultDTO approve(PoReturnEntity entity, String type, String comment, Boolean isNeedProcess,List<PoReturnDetailEntity> poReturnDetailList);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param entity
     * @return java.lang.Boolean
     **/
    BatchResultDTO disApprove(PoReturnEntity entity,List<PoReturnDetailEntity> detailEntityList);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @param remark remark
     * @return java.lang.Boolean
     **/
    Boolean invalid(List<String> ids, String remark);

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> ids);

    /**
     * 导出
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    Boolean exportExcel(@RequestBody PurchaseReturnOrderDTO.PagingParamDTO dto);


    /**
     * 采购订单-关联的退货订单
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     **/
    List<PurchaseReturnOrderDTO.OrderRefReceiveDTO> purchaseOrderRefReturn(String purchaseOrderId);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>>
     **/
    List<PurchaseReturnOrderDTO.ReturnOrderCountDTO> listCount(@RequestBody PermissionsDTO dto);
    /**
     * @description: 根据来源id查询
     * @author Will
     * @date: 2023/4/19 9:27
     * @param sourceIds
     * @return List<PurchaseReturnOrderEntity>
     */
    List<PoReturnEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 获取退货数量
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     **/
    List<PurchaseReturnOrderDTO.GetReturnQtyDTO> getReturnQty(String purchaseOrderId);

    /**
     * 修改金蝶同步状态
     * @Author Luo_WG
     * @Date 2023/4/24 15:29
     * @param id
     * @param syncKingdeeId
     * @return java.lang.Boolean
     **/
    Boolean updateSyncKingdeeId(String id,String syncKingdeeId);

    
    /**
     * 批量生成退货单
     * @author yl
     * @date 2023-04-25 11:09
     * @param list
     * @return java.lang.Boolean
     */
    Boolean batchAdd(List<PurchaseReturnOrderDTO.AddDTO> list);

    /**
     * 修改到货状态
     * @Author Luo_WG
     * @Date 2023/4/28 11:41
     * @param purchaseOrderDetailIds
     * @return void
     **/
    void updateArrivalState(List<String> purchaseOrderDetailIds);

    
    /**
     * 下推 退货单
     * @author yl
     * @date 2023-05-08 11:05
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean generatePurchaseReturnOrder(PoInstockDTO.ListGeneratePurchaseReturnOrderDTO dto);

    /**
     * 根据供应商id集合、单据日期等条件 查询退货数量信息
     * @param params
     * @return
     */
    List<PurchaseReturnOrderDTO.SupplierReturnDTO> getReturnInfo(PurchaseReturnOrderDTO.SupplierReturnParamDTO params);

    /**
     * 验证SKU是否缺货
     * @param dto
     * @return
     */
    String checkSkuInventory(PurchaseReturnOrderDTO.AddDTO dto, List<PurchaseReturnOrderDetailDTO.AddDTO> detailList);
    /**
     * @description: 下推自动生成采购订单
     * @author Will
     * @date: 2023/8/7 10:51
     * @param dto
     * @return Boolean
     */
    Boolean autoGeneratePurchaseOrder(BaseIdsDTO.IdsDTO dto);

    /**
     * PDA:列表查询
     * @Author Luo_WG
     * @Date 2023/8/21 12:25
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PdaPagingViewDTO>
     **/
    PagingVO<PurchaseReturnOrderDTO.PdaPagingViewDTO> pdaPaging(PagingDTO<PurchaseReturnOrderDTO.PdaPagingParamDTO> dto);

    /**
     * pda:列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/21 16:14
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PdaReturnOrderCountDTO>
     **/
    List<PurchaseReturnOrderDTO.PdaReturnOrderCountDTO> pdaListCount(PermissionsDTO dto);

    /**
     * PDA:新增
     * @Author Luo_WG
     * @Date 2023/8/30 16:16
     * @param dto
     * @return java.lang.String
     **/
    String pdaAdd(PurchaseReturnOrderDTO.AddDTO dto);

    /**
     * PDA:修改
     * @Author Luo_WG
     * @Date 2023/8/30 16:17
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean pdaUpdate(PurchaseReturnOrderDTO.UpdateDTO dto);

    /**
     * PDA:新增并提交
     * @Author Luo_WG
     * @Date 2023/8/30 18:28
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean pdaAddAndSubmit(PurchaseReturnOrderDTO.AddDTO dto);

    /**
     * 修改并提交
     * @Author Luo_WG
     * @Date 2023/8/30 18:28
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean pdaUpdateAndSubmit(PurchaseReturnOrderDTO.UpdateDTO dto);

    /**
     * 修复退货来源错误数据
     * @Author Luo_WG
     * @Date 2023/10/12 10:22
     * @return java.lang.Boolean
     **/
    Boolean dataRepairTemp();

    /**
     * SRM供应商退货单列表查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>
     **/
    PagingVO<PurchaseReturnOrderDTO.SupplierPagingViewDTO> supplierPaging(PagingDTO<PurchaseReturnOrderDTO.SupplierPagingParamDTO> pagingParamDTO);

    /**
     * SRM供应商退货列表tab页
     * @Author Luo_WG
     * @Date 2024/1/11 17:58
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.SupplierTabListDTO>
     **/
    List<PurchaseReturnOrderDTO.SupplierTabListDTO> supplierTabList(PermissionsDTO dto);

    /**
     * 退货确认
     * @Author Luo_WG
     * @Date 2024/1/11 18:24
     * @param id
     * @return java.lang.Boolean
     **/
    BatchResultDTO returnConfirm(String id);

    /**
     * 异常反馈
     * @Author Luo_WG
     * @Date 2024/1/11 18:50
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean unusualFeedback(PurchaseReturnOrderDTO.UnusualFeedbackParamDTO dto);

    /**
     * 异常处理人下拉接口
     * @Author Luo_WG
     * @Date 2024/1/12 11:17
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.UnusualHandleUserOptionDTO>>
     **/
    List<PurchaseReturnOrderDTO.UnusualHandleUserOptionDTO> unusualHandleUserOption();

    /**
     * 异常反馈详情页
     * @Author Luo_WG
     * @Date 2024/1/12 12:35
     * @param id
     * @return com.erp.model.wms.dto.PurchaseReturnOrderDTO.UnusualFeedbackParamDTO
     **/
    PurchaseReturnOrderDTO.UnusualFeedbackView unusualFeedbackView(String id);

    /**
     * 查询审核通过待确认的单据
     * @Author Luo_WG
     * @Date 2024/1/12 14:54
     * @return com.erp.model.wms.dto.PurchaseReturnOrderDTO.UnusualFeedbackView
     **/
    List<PoReturnEntity> listByApproceAndWaitConfirm();


    PurchaseReturnStatisticsDTO.ResponseDTO statisticsBySupplier(PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO);

    /**
     * 退货单定时器自动确认
     * @Author Luo_WG
     * @Date 2024/1/24 12:40
     * @return void
     **/
    void poReturnAutoConfirm();

    PurchaseReturnStatisticsDTO.StatusDTO confirmStatusCountBySupplier(PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO);

    PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> exportPurchaseReturnOrder(PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> dto);

    /**
     * 获取委外订单列表
     * @param purchaseOrderId
     * @return
     */
    List<PurchaseReturnOrderDTO.SubcontractOrderDTO> listSubcontractOrder(String purchaseOrderId);
}
