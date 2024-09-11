package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/17 15:56
 */
@FeignClient(name = "erp-wms",configuration = {FeignErrorDecoder.class})
public interface WmsTaskFeign {



    /**
     * 根据仓库id
     */
    @PostMapping("feign/warehouse/listWarehouseByIds")
    List<WarehouseDTO.UpdateDTO> listWarehouseByIds(@RequestBody List<String> warehouseIds);

    /**
     * 根据仓库名称查询
     */
    @PostMapping("feign/warehouse/listWarehouseByNameList")
    List<WarehouseDTO.UpdateDTO> listWarehouseByNameList(@RequestBody List<String> warehouseNameList);

    /**
     * 查询所有审核通过并启用的仓库
     */
    @GetMapping("feign/warehouse/listApproveWarehouse")
    List<WarehouseDTO.UpdateDTO> listApproveWarehouse();

    /**
     * 根据采购订单明细ids查询收货明细
     */
    @PostMapping("feign/warehouseReceive/listWarehouseReceiveByPodIds")
    List<WarehouseReceiveDetailEntity> listWarehouseReceiveDetailByPodIds(@RequestBody List<String> purchaseDetailIds);

    /**
     * 根据来源明细ids查询退货明细
     */
    @PostMapping("feign/purchaseReturnOrder/listDetailBySourceDetailIds")
    List<PoReturnDetailEntity> listPurchaseReturnOrderDetailBySourceDetailIds(List<String> sourceDetailIds);

    /**
     * 根据来源明细ids查询退货明细
     */
    @PostMapping("feign/purchaseReturnOrder/listPurchaseReturnOrderDetailByMainIds")
    List<PoReturnDetailEntity> listPurchaseReturnOrderDetailByMainIds(List<String> mainIds);

    /**
     * 根据来源明细ids查询入库明细
     */
    @PostMapping("feign/purchaseStockIn/listDetailBySourceDetailIds")
    List<PoInstockDetailEntity> listPurchaseStockInDetailBySourceDetailIds(List<String> sourceDetailIds);

    /**
     * 根据来采购订单明细ids查询入库明细
     */
    @PostMapping("feign/purchaseStockIn/listDetailByPodIds")
    List<PoInstockDetailEntity> listPurchaseStockInDetailByPodIds(List<String> PodIds);

    /**
     * 批量新增入库单
     */
    @PostMapping("feign/purchaseStockIn/batchAddPurchaseStockIn")
    Boolean batchAddPurchaseStockIn(List<PoInstockDTO.AddDTO> resultList);

    /**
     * 批量新增入库单
     */
    @PostMapping("feign/warehouseReceive/addWarehouseReceive")
    String addWarehouseReceive(WarehouseReceiveDTO.AddDTO dto);

    /**
     * 批量新增退货单
     */
    @PostMapping("feign/purchaseReturnOrder/addReturnOrder")
    Boolean batchAddReturnOrder(List<PurchaseReturnOrderDTO.AddDTO> dto);

    /**
     * 获取入库数量
     **/
    @PostMapping("feign/purchaseStockIn/getStockInQty")
    List<PoInstockDTO.GetStockInQty> getStockInQty(@RequestBody List<String> ids);

    /**
     * 获取退货数量
     **/
    @PostMapping("feign/purchaseReturnOrder/listReturnOrderDetailByPodIds")
    List<PoReturnDetailEntity> listReturnOrderDetailByPodIds(@RequestBody List<String> ids);

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("feign/wmsWorkOption/getTableNum")
    List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(@RequestBody List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList);

    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/syncKingdee/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);

    /**
     * 采购收货审核
     * @Author Luo_WG
     * @Date 2023/5/17 10:51
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/wmsWorkOption/warehouseReceiveApprove")
    List<BatchResultDTO> warehouseReceiveApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 采购入库审核
     * @author Will
     * @date: 2023/4/11 20:11
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("feign/wmsWorkOption/poInstockApprove")
    List<BatchResultDTO> poInstockApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 采购退货审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("feign/wmsWorkOption/purchaseReturnOrderApprove")
    List<BatchResultDTO> purchaseReturnOrderApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 调拨申请单审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("feign/wmsWorkOption/transferApplicationApprove")
    List<BatchResultDTO> transferApplicationApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 盘点任务审核通过
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param approveOneDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("feign/wmsWorkOption/stocktakingTaskApprove")
    Boolean stocktakingTaskApprove(@RequestBody @Validated BaseApproveParamDTO approveOneDTO);

    /**
     * 根据销售 销售订单详情ids 获取是否有下推的单据
     * @author yl
     * @date 2023-05-25 10:27
     * @param soDetailIdList
     * @return java.lang.Integer
     */
    @PostMapping("feign/soDeliveryNotice/getPushDownBySoDetailIds")
    Integer getPushDownBySoDetailIds(@RequestBody List<String> soDetailIdList);


    /**
     * 根据销售订单ids 获取是否有下推的单据
     * @author yl
     * @date 2023-05-25 10:27
     * @param soIds
     * @return java.lang.Integer
     */
    @PostMapping("feign/soDeliveryNotice/getPushDownBySourceIds")
    Integer getPushDownBySourceIds(@RequestBody List<String> soIds);

    /**
     * 关闭关联单据的关闭状态
     * @author yl
     * @date 2023-05-25 19:25
     * @param terminateSoDetailIds
     * @return void
     */
    @PostMapping("feign/soDeliveryNotice/closeBySoDetailIds")
    void closeBySoDetailIds(@RequestBody List<String> terminateSoDetailIds);

    /**
     * 根据供应商id集合获取收货单量和收货数量
     * @param dto
     * @return List<WarehouseReceiveDTO.SupplierReceiveInfoDTO>
     */
    @PostMapping("feign/warehouseReceive/getReceiveInfoBySupplierIds")
    List<WarehouseReceiveDTO.SupplierReceiveInfoDTO> getReceiveInfoBySupplierIds(@RequestBody WarehouseReceiveDTO.SupplierReceiveParamDTO dto);

    /**
     * 根据供应商id集合获取收货单量和收货数量
     * @param dto
     * @return List<WarehouseReceiveDTO.SupplierReceiveInfoDTO>
     */
    @PostMapping("feign/warehouseReceive/listReceiveBySourceTypeAndIds")
    List<WarehouseReceiveEntity> listReceiveBySourceTypeAndIds(@RequestBody WarehouseReceiveDTO.SourceParamDTO dto);

    /**
     * 根据供应商id集合获取入库单量和入库数量
     * @param dto
     * @return List<PoInstockDTO.SupplierInstockInfoDTO>
     */
    @PostMapping("feign/purchaseStockIn/getInstockInfoBySupplierIds")
    List<PoInstockDTO.SupplierInstockInfoDTO> getInstockInfoBySupplierIds(@RequestBody PoInstockDTO.SupplierInstockParamDTO dto);

    /**
     * 根据供应商id集合获取入库单量和入库数量
     * @param dto
     * @return List<PurchaseReturnOrderDTO.SupplierReturnDTO>
     */
    @PostMapping("feign/purchaseReturnOrder/getReturnInfo")
    List<PurchaseReturnOrderDTO.SupplierReturnDTO> getReturnInfo(@RequestBody PurchaseReturnOrderDTO.SupplierReturnParamDTO dto);

    /**
     * 根据采购订单获取质检信息
     * @param dto
     * @return List<PurchaseReturnOrderDTO.SupplierReturnDTO>
     */
    @PostMapping("/feign/qcBill/getQcInfoByPurchaseOrder")
    QcInfoDTO.PurchaseQcInfoDTO getQcInfoByPurchaseOrder(@RequestBody QcInfoDTO.PurchaseQcParamDTO dto);


    /**
     * 根据采购订单明细id获取质检信息
     * @param purchaseDetailIds
     * @return List<PurchaseReturnOrderDTO.SupplierReturnDTO>
     */
    @PostMapping("/feign/qcBill/getQcReceiveResult")
    List<QcInfoDTO.QcReceiveResultDTO> getQcReceiveResult(@RequestBody List<String> purchaseDetailIds);

    /**
     * 根据金蝶仓库code 获取到对应仓库信息
     * @Author Luo_WG
     * @Date 2023/6/28 9:45
     * @param kingdeeWarehouseCodeList kingdeeWarehouseCodeList
     * @return java.util.List<com.erp.model.wms.entity.WarehouseEntity>
     **/
    @PostMapping("feign/warehouse/listByKingdeeCodeList")
    List<WarehouseEntity> listByKingdeeCodeList(@RequestBody List<String> kingdeeWarehouseCodeList);
    /**
     * @description: 根据id查询采购退货单数据
     * @author Will
     * @date: 2023/9/5 14:24
     * @param poReturnIdList
     * @return List<PurchaseReturnOrderEntity>
     */
    @PostMapping("feign/purchaseReturnOrder/listPoReturnByIdList")
    List<PoReturnEntity> listPoReturnByIdList(@RequestBody List<String> poReturnIdList);

    @PostMapping("feign/machineInfo/listBySku")
    List<MachineInfoDTO.ListDTO> listBySku(@RequestBody MachineInfoDTO.FindInfoBySkuDTO dto);

    /**
     * wms同步数据
     * @Author Luo_WG
     * @Date 2023/10/30 12:22
     * @param syncParamDTO
     * @return void
     **/
    @PostMapping("/feign/wmsSyncTask/findDataSendSyncTask")
    void findDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO);

    /**
     * wms同步马帮数据
     * @Author Luo_WG
     * @Date 2023/10/30 12:22
     * @param syncParamDTO
     * @return void
     **/
    @PostMapping("/feign/wmsSyncTask/findMaBangDataSendSyncTask")
    void findMaBangDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO);

    /**
     * FBA发货单审核通过
     * @Author Luo_WG
     * @Date 2023/11/15 18:01
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/wmsWorkOption/fbaDeliveryApprove")
    Boolean fbaDeliveryApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 海外发货计划审核通过
     * @Author Luo_WG
     * @Date 2023/11/17 16:16
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/wmsWorkOption/overseasDeliveryPlanApprove")
    Boolean deliveryPlanApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 统计供应商周期内已审核订单数量
     * @param supplierId
     * @return
     */
    @GetMapping("/feign/warehouseReceive/countOrderBySupplierId")
    SupplierCountDTO countOrderBySupplierId(@RequestParam("supplierId") String supplierId);
    /***
     * 同销售订单获取 收货详情
     * @param purchaseOrderIds
     * @return
     */
    @PostMapping("/feign/warehouseReceive/getReceiveListByPurchaseOrderIds")
    public List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIds(@RequestBody List<String> purchaseOrderIds);


    /***
     * @return
     */
    @PostMapping("/feign/warehouseReceive/getReceiveListByPurchaseOrderIdsAll")
    List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIdsAll(@RequestBody List<String> purchaseOrderIds);

    /**
     * WMS同步旺店通数据
     * @param syncParamDTO
     * @return
     * @date: 2024-08-15
     * @author: tanmujin
     */
    @PostMapping("/feign/wmsSyncTask/findWdtDataSendSyncTask")
    void findWdtDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO);

    /**
     * 直接调拨单审核
     * @param baseApproveParamDTO
     * @return
     */
    @PostMapping("feign/wmsWorkOption/transferInfoApprove")
    List<BatchResultDTO> transferInfoApprove(@RequestBody BaseApproveParamDTO baseApproveParamDTO);
}