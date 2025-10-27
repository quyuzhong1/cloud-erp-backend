package com.erp.server.scm.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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
     * @param entity
     * @return Boolean
     */
    BatchResultDTO delete(PurchaseOrderEntity entity);
    /**
     * @description: 批量审核
     * @author Will
     * @date: 2023/3/16 11:26
     * @param approveOneDTO
     */
    BatchResultDTO approve(ApproveOneDTO approveOneDTO);

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/11 14:21
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, PurchaseOrderEntity entity);

    /**
     * @description: 批量反审核
     * @author Will
     * @date: 2023/3/16 11:26
     * @param id
     * @return Boolean
     */
    BatchResultDTO disApprove(String id);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/3/16 11:27
     * @param entity
     * @return Boolean
     */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto, PurchaseOrderEntity entity);

    /**
     * 根据ids查询
     */
    List<PurchaseOrderEntity> getList(List<String> ids);

    /**
     * @description: 导出采购合同PDF
     * @author Will
     * @date: 2023/3/16 11:42
     * @param id
     * @return PurchaseOrderDTO.ExportPdfDTO
     */
    PurchaseOrderDTO.ExportPdfDTO listPurchaseContractPdf(String id);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/3/16 11:58
     * @param excelImportDTO
     * @param response
     * @return PurchaseOrderDetailDTO.ImportDTO
     */
    PurchaseOrderDetailDTO.ImportDTO importFile(ExcelImportDTO.purchaseOrderExcelImportDTO excelImportDTO, HttpServletResponse response);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/3/16 11:58
     */
    Boolean exportExcel(PurchaseOrderDTO.SearchParamDTO dto);
    /**
     * @param entity
     * @return Boolean
     * @description: 提交
     * @author Will
     * @date: 2023/3/16 16:11
     */
    BatchResultDTO submit(PurchaseOrderEntity entity, Boolean isStartProcess);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/3/17 12:59
     * @param dto
     * @return Boolean
     */
    BatchResultDTO addAndSubmit(PurchaseOrderDTO.AddDTO dto);
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
    BatchResultDTO invalid(PurchaseOrderEntity entity, String remark);
    /**
     * @description: 下推签收单弹框数据显示
     * @author Will
     * @date: 2023/3/29 15:55
     * @param purchaseDetailIdList
     * @return List<ViewGenerateReceiveDTO>
     */
    List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewGenerateReceive(List<String> purchaseDetailIdList);

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
     * @param purchaseDetailIdList
     * @return List<ViewGenerateStockInDTO>
     */
    List<PurchaseOrderDTO.ViewGenerateStockInDTO> viewGenerateStockIn(List<String> purchaseDetailIdList);
    /**
     * @description: 根据id查询采购订单
     * @author Will
     * @date: 2023/4/17 9:20
     * @param id
     * @return UpdateDTO
     */
    PurchaseOrderDTO.GetOneDTO getPurchaseOrder(String id);

    /**
     * 根据采购订单明细id 获取对应产品信息
     * @author yl
     * @date 2023-04-17 18:27
     * @param purchaseOrderId
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetQcProductDTO
     */
    PurchaseOrderDTO.GetQcProductDTO getQcProductInfo(String purchaseOrderId);

    /**
     * 根据采购订单明细id 获取对应产品信息
     * @author jack
     * @date 2025-03-27
     * @param purchaseOrderDetailId
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetQcProductDTO
     */
    PurchaseOrderDTO.GetQcProductDTO getQcProductInfoByDetailId(String purchaseOrderDetailId);
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
     * @param purchaseDetailIdList
     * @return java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>
     */
    List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> purchaseDetailIdList);

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
     *
     * @param id
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/7/13 15:09
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
     * @param queryPurchaseDTO
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    List<SkuCostDTO> listPurchaseOrderByPurchaseDate(SkuCostDTO.QueryPurchaseDTO queryPurchaseDTO);
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
     * @return BatchResultDTO
     */
    BatchResultDTO supplierConfirm(String id);

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
     * srm订单确认列表合计
     * @param dto
     * @return
     */
    PurchaseOrderDTO.ListDTO srmOrderConfirmTotal(PurchaseOrderDTO.SrmSearchParamDTO dto);

    /**
     * 生成送货单
     * @param dto
     * @return
     */
    List<PurchaseOrderDTO.ListDTO> generateDeliveryList(PurchaseOrderSrmDTO.GenerateDeliveryParamDTO dto);

    PurchaseStatisticsDTO.StatusDTO statisticsExecutionStatus(PurchaseStatisticsDTO.RequestDTO requestDTO);

    /**
     * 导入结束交货
     * @author Will
     * @date: 2024/3/5 11:12
     * @param multipartFile
     * @param response
     */
    Boolean importEndReceiveFile(MultipartFile multipartFile, HttpServletResponse response);

    /**
     * 导出srm供应商采购订单
     * @param dto
     * @param response
     * @return
     */
    Boolean exportSrmExcel(PurchaseOrderDTO.SrmSearchParamDTO dto, HttpServletResponse response);

    SupplierUserInfoVO getSrmSupplierUserInfo();

    /**
     * 待发货 时间周期统计
     * @param dto
     * @return
     */
    List<DeliveryOrderDTO.WaitDeliveryCountDTO> srmWaitDeliveryCount(PurchaseOrderSrmDTO.WaitDeliveryParamDTO dto);

    /**
     * 待发货 分页列表
     * @param dto
     * @return
     */
    PagingVO<PurchaseOrderDTO.ListDTO> srmWaitDeliveryPaging(PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto);
    /**
     * 待发货订单合计
     * @param dto
     * @return
     */
    PurchaseOrderDTO.ListDTO srmWaitDeliveryTotal(PurchaseOrderDTO.SrmSearchParamDTO dto);

    PagingVO<BomExportExcelVO> exportPurchaseOrderContract(PagingDTO<String> dto);

    PagingVO<PurchaseOrderDTO.ListDTO> exportPurchaseOrder(PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto);
    /**
     * 导出采购合同pdf
     * @author will
     * @date 2024/11/5 10:33
     * @param id
     * @param response
     */
    void exportPurchaseContractPdf(String id, HttpServletResponse response);
    /**
     * 采购编码分页查询
     * @author will
     * @date 2024/11/11 11:39
     * @param dto
     * @return PagingVO<SourceCodeDTO>
     */
    PagingVO<PurchaseOrderDTO.SourceCodeDTO> purchaseCodePaging(PagingDTO<PurchaseOrderDTO.SourceCodeParamDTO> dto);

    /**
     * 通过sku 供应商查询所有采购订单
     * @param purchaseCalcQtyParamsDTO 参数
     */
    List<PurchaseOrderDTO.PurchaseCalcQtyDTO> listAllPurchaseBySkuIdAndSupplier(PurchaseOrderDTO.PurchaseCalcQtyParamsDTO purchaseCalcQtyParamsDTO);

    /**
     * 合同状态更新
     * @author jack
     * @date: 2025/5/12
     * @param dto
     * @return ApiResult
     */
    void updateContractStampStatus(PurchaseOrderDTO.ContractStampStatusParamsDTO dto);

    /**
     * 定时任务 ： (供应商 + 采购订单 + sku )采购数量计算
     */
    void calSupplierPurchaseQty();

    List<PurchaseOrderDTO.SupplierSkuDTO> listSkuBySupplierIds(List<String> supplierIds);
    /**
     * 历史未完结订单分页查询
     * @author will
     * @date 2025/7/29 15:15
     * @param dto
     * @return PagingVO<AdjustListDTO>
     */
    PagingVO<PurchaseOrderDTO.AdjustListDTO> adjustPaging(PagingDTO<PurchaseOrderDTO.SearchAdjustParamDTO> dto);
    /**
     * 导出历史未完结订单
     * @author will
     * @date 2025/7/29 19:09
     * @param dto
     * @return Boolean
     */
    Boolean exportAdjustExcel(PurchaseOrderDTO.SearchAdjustParamDTO dto);
    /**
     * 批量调价
     * @author will
     * @date 2025/7/30 09:24
     * @param dto
     * @return Boolean
     */
    Boolean batchAdjustPrice(PurchaseOrderDTO.AdjustPriceDTO dto);
    /**
     * 导入采购订单主表信息
     * @author will
     * @date 2025/7/30 18:27
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importMainFile(MultipartFile excelFile, HttpServletResponse response);
}
