package com.erp.server.scm.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 采购订单表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseOrderService extends SuperService<PurchaseOrderEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/16 11:21
     * @param dto
     * @return PagingVO<PurchaseOrderDTO.listDTO>
     */
    PagingVO<PurchaseOrderDTO.ListDTO> paging(PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto);
    PagingVO<PurchaseOrderDTO.ListDTO> srmOrderConfirmPaging(PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/16 11:22
     * @param dto
     * @return PurchaseOrderEntity
     */
    PurchaseOrderEntity add(PurchaseOrderDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/3/16 11:23
     * @param dto
     * @return Boolean
     */
    Boolean update(PurchaseOrderDTO.UpdateDTO dto);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/3/16 11:23
     * @param id
     * @return PurchaseOrderDTO.viewDTO
     */
    PurchaseOrderDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/3/16 11:23
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 批量审核
     * @author Will
     * @date: 2023/3/16 11:26
     * @param baseApproveParamDTO
     */
    Boolean approve(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/11 14:21
     * @param dto
     * @param list
     * @return Boolean
     */
    Boolean approveEnd(BaseApproveParamDTO dto, List<PurchaseOrderEntity> list);

    /**
     * @description: 批量反审核
     * @author Will
     * @date: 2023/3/16 11:26
     * @param ids
     * @return Boolean
     */
    Boolean disApprove(List<String> ids);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/3/16 11:27
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 结束交货
     * @author Will
     * @date: 2023/3/16 11:35
     * @param ids
     * @param remark
     * @param isValid
     * @return Boolean
     */
    Boolean finishDelivery(List<String> ids, String remark,Boolean isValid);

    /**
     * @description: 导出采购合同PDF
     * @author Will
     * @date: 2023/3/16 11:42
     * @param id
     * @return PurchaseOrderDTO.ExportPdfDTO
     */
    PurchaseOrderDTO.ExportPdfDTO exportPurchaseContractPdf(String id);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/3/16 11:58
     * @param excelFile
     * @param response
     * @return PurchaseOrderDetailDTO.ImportDTO
     */
    PurchaseOrderDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds,String supplierId, HttpServletResponse response);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/3/16 11:58
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(PurchaseOrderDTO.SearchParamDTO dto, HttpServletResponse response);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/3/16 16:11
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids,Boolean isStartProcess);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/3/17 12:59
     * @param dto
     * @return Boolean
     */
    Boolean addAndSubmit(PurchaseOrderDTO.AddDTO dto);
    /**
     * @description:
     * @author Will
     * @date: 2023/3/27 15:51
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(PurchaseOrderDTO.UpdateDTO dto);
    /**
     * @description: 查询列表数量
     * @author Will
     * @date: 2023/3/29 9:59
     * @return List<PurchaseOrderCountDTO>
     */
    List<ListStatusCountDTO.PurchaseOrderCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 批量作废
     * @author Will
     * @date: 2023/3/29 10:28
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 下推签收单弹框数据显示
     * @author Will
     * @date: 2023/3/29 15:55
     * @param ids
     * @return List<ViewGenerateReceiveDTO>
     */
    List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewGenerateReceive(List<String> ids);

    /**
     * @description: 采购变更数据显示
     * @author Will
     * @date: 2023/3/31 14:29
     * @param ids
     * @return ViewDTO
     */
    PurchaseChangeDTO.ViewDTO viewPurchaseChange(List<String> ids);
    /**
     * @description: 下推采购入库单显示
     * @author Will
     * @date: 2023/4/13 11:38
     * @param ids
     * @return List<ViewGenerateStockInDTO>
     */
    List<PurchaseOrderDTO.ViewGenerateStockInDTO> viewGenerateStockIn(List<String> ids);
    /**
     * @description: 根据id查询采购订单
     * @author Will
     * @date: 2023/4/17 9:20
     * @param id
     * @return UpdateDTO
     */
    PurchaseOrderDTO.GetOneDTO getPurchaseOrder(String id);

    /**
     * 根据采购订单id 获取对应产品信息
     * @author yl
     * @date 2023-04-17 18:27
     * @param purchaseOrderId
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetQcProductDTO
     */
    PurchaseOrderDTO.GetQcProductDTO getQcProductInfo(String purchaseOrderId);
    /**
     * @description: 更新金蝶发送状态
     * @author Will
     * @date: 2023/4/21 10:04
     * @param syncKingdeeId
     * @return Boolean
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * 根据采购订单id 集合获取对应数量
     * @author yl
     * @date 2023-04-23 14:03
     * @param purchaseOrderIds
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO>
     */
    List<PurchaseOrderDTO.PurchaseOrderInfoDTO> getPurchaseOrderByOrderIds(List<String> purchaseOrderIds);


    /**
     * 采购订单 下推 退货数据显示
     * @author yl
     * @date 2023-04-25 9:39
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>
     */
    List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> ids);

    /**
     * @description: 根据来源明细ids查询
     * @author Will
     * @date: 2023/6/13 15:36
     * @param sourceDetailIds
     * @return List<ListDTO>
     */
    List<PurchaseOrderDTO.ListDTO> listBySourceDetailIds(List<String> sourceDetailIds);

    /**
     * @description: 列表数据处理
     * @author Will
     * @date: 2023/6/13 15:57
     * @param records
     */
    void doOpHandlePurchaseOrder(List<PurchaseOrderDTO.ListDTO> records);

    /**
     * 根据采购订单编号获取采购订单信息
     * @param codes
     * @return
     */
    List<PurchaseOrderEntity> findByCodes(List<String> codes);
    /**
     * @description: 根据采购订单id查询委外订单下所有的采购订单id
     * @author Will
     * @date: 2023/6/15 15:10
     * @param poId
     * @return List<ViewSubcontractPoDTO>
     */
    List<PurchaseOrderDTO.ViewSubcontractPoDTO> viewSubcontractPo(String poId);
    /**
     * @description: 查询委外订单所有子级SKU生成的采购订单信息
     * @author Will
     * @date: 2023/6/15 17:49
     * @param parentPodIds
     * @return List<SubcontractOrderChildDTO>
     */
    List<PurchaseOrderDTO.SubcontractOrderChildDTO> listPoRefSubChildByParentPodIds(List<String> parentPodIds);
    /**
     * @description: 自动审核采购订单
     * @author Will
     * @date: 2023/6/15 18:51
     * @param poIds
     */
    void autoApprovePurchaseOrder(List<String> poIds);
    /**
     * @description: 来源ids
     * @author Will
     * @date: 2023/6/19 14:45
     * @param sourceIds
     * @return List<PurchaseOrderEntity>
     */
    List<PurchaseOrderEntity> listBySourceIds(List<String> sourceIds);
    /**
     * @description: 更新申请单创建采购订单类型
     * @author Will
     * @date: 2023/6/25 11:44
     * @param purchaseOrderIds
     */
     void updateCreatePoType(List<String> purchaseOrderIds);
    /**
     * 导出网采合同
     * @Author Luo_WG
     * @Date 2023/7/13 15:09
     * @param id
     * @param response
     * @return java.lang.Boolean
     **/
    Boolean exportPurchaseContract(String id, HttpServletResponse response);
    /**
     * @description: 列表查询总数
     * @author Will
     * @date: 2023/7/17 15:05
     * @param dto
     * @return PagingTotalDTO
     */
    PurchaseOrderDTO.PagingTotalDTO pagingTotal(PurchaseOrderDTO.SearchParamDTO dto);
    /**
     * @description: 更新明细备注
     * @author Will
     * @date: 2023/7/19 14:59
     * @param dto
     * @return Boolean
     */
    Boolean updateRemark(BaseIdsDTO.RemarkDTO dto);

    /**
     * @description: 根据来源id查询采购订单数据
     * @author Will
     * @date: 2023/8/7 11:05
     * @param sourceIds
     * @return List<PurchaseOrderEntity>
     */
    List<PurchaseOrderEntity> listPoBySourceIds(List<String> sourceIds);

    /**
     * 根据sku编号查询采购单
     * @Author Luo_WG
     * @Date 2023/8/11 10:23
     * @param dto
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    List<PurchaseOrderDTO.PdaPurchaseOrder> pdaList(PurchaseOrderDTO.PdaPurchaseOrderParam dto);

    /**
     * PDA:查询详情
     * @Author Luo_WG
     * @Date 2023/8/11 14:39
     * @param id
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.ViewDTO
     **/
    PurchaseOrderDTO.PdaViewDTO pdaView(String id);

    /**
     * PDA:根据sku编号查询采购单所有审核通过的信息
     * @Author Luo_WG
     * @Date 2023/9/1 9:05
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.PdaPurchaseOrder>
     **/
    List<PurchaseOrderDTO.PdaPurchaseOrder> pdaListAll(PurchaseOrderDTO.PdaPurchaseOrderParam dto);

    /**
     * 根据采购日期查询采购采购单
     * @Author Luo_WG
     * @Date 2023/9/13 18:21
     * @param purchaseDateList
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    List<SkuCostDTO> listPurchaseOrderByPurchaseDate(List<LocalDate> purchaseDateList);
    /**
     * @description: 根据skuId集合查询采购成本数据
     * @author Will
     * @date: 2023/11/23 16:24
     * @param paramDTO
     * @return List<SkuCostDTO>
     */
    List<SkuCostDTO> listPurchaseOrderCost(SkuCostDTO.ParamDTO paramDTO);

    PurchaseStatisticsDTO.ResponseDTO statisticsBySupplier(PurchaseStatisticsDTO.RequestDTO requestDTO);

    /**
     * SRM 订单确认列表统计
     * @param dto
     * @return
     */
    List<ListStatusCountDTO.PurchaseOrderConfirmCountDTO> srmOrderConfirmCount(PurchaseOrderSrmDTO.SearchParamDTO dto);
    /**
     * @description: 供应商确认
     * @author Will
     * @date: 2024/1/16 17:10
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO supplierConfirm(String id,String remark);

    /**
     * 批量接受/拒绝
     * @param dto
     * @return
     */
    List<BatchResultDTO> srmOrderConfirmStatus(PurchaseOrderDTO.ConfirmDTO dto);
    /**
     * @description: 定时任务自动确认
     * @author Will
     * @date: 2024/1/17 10:28
     */
    void purchaseOrderAutoConfirm();

    /**
     * 待发货 时间周期统计
     * @param dto
     * @return
     */
    PurchaseOrderSrmDTO.WaitDeliveryCountDTO srmWaitDeliveryCount(PurchaseOrderSrmDTO.WaitDeliveryParamDTO dto);

    /**
     * 待发货 分页列表
     * @param dto
     * @return
     */
    PagingVO<PurchaseOrderDTO.ListDTO> srmWaitDeliveryPaging(PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto);

    /**
     * srm订单确认列表合计
     * @param dto
     * @return
     */
    PurchaseOrderDTO.ListDTO srmOrderConfirmTotal(PurchaseOrderDTO.SrmSearchParamDTO dto);
}
