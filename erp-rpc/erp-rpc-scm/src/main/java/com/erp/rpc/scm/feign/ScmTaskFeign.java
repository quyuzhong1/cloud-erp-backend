package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.*;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 采购订单feign
 * @Author Luo_WG
 * @Date 2023/4/13 11:41
 **/
@FeignClient(name = "erp-scm",contextId = "scmTaskFeign",configuration = {FeignErrorDecoder.class})
public interface ScmTaskFeign {

    /**
     * 根据id查询采购订单
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @PostMapping("feign/purchaseOrder/getPurchaseOrderById")
    PurchaseOrderEntity getPurchaseOrderById(@RequestBody String id);

    /**
     * 根据ids查询采购订单
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @PostMapping("feign/purchaseOrder/listPurchaseOrderByIds")
    List<PurchaseOrderEntity> listPurchaseOrderByIds(@RequestBody List<String> ids);



    /**
     * 根据采购订单id查询供应商
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("feign/purchaseOrder/getOrderSupplierByOrderId")
    PurchaseOrderSupplierEntity getOrderSupplierByOrderId(@RequestBody String id);

    /**
     * @description: 根据采购订单id集合查询供应商
     * @author Will
     * @date: 2024/1/25 10:15
     * @param idList
     * @return List<PurchaseOrderSupplierEntity>
     */
    @PostMapping("feign/purchaseOrder/listOrderSupplierByOrderIdList")
    List<PurchaseOrderSupplierEntity> listOrderSupplierByOrderIdList(@RequestBody List<String> idList);

    /**
     * 根据采购订单详情id查询详情信息
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id：采购订单详情表id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @PostMapping("feign/purchaseOrder/listPurchaseOrderDetailById")
    List<PurchaseOrderDetailEntity> listPurchaseOrderDetailById(@RequestBody List<String> id);
    /**
     * @description: 根据id查询供应商
     * @author Will
     * @date: 2023/4/14 10:07
     * @param supplierId
     * @return SupplierEntity
     */
    @PostMapping("feign/purchaseOrder/getSupplierById")
    SupplierEntity getSupplierById(@RequestBody String supplierId);

    /**
     * 根据ids查询供应商
     * @description:
     * @author Will
     * @date: 2023/4/14 10:07
     * @param supplierIds
     * @return SupplierEntity
     */
    @PostMapping("feign/purchaseOrder/getSupplierByIdList")
    List<SupplierEntity> getSupplierByIdList(@RequestBody List<String> supplierIds);

    /**
     * 根据供应商名称 获取供应商
     * @author yl
     * @date 2023-09-22 19:49
     * @param supplierNames
     * @return java.util.List<com.erp.model.scm.entity.SupplierEntity>
     */
    @PostMapping("feign/supplier/listBySupplierByNames")
    List<SupplierEntity> listBySupplierByNames(@RequestBody List<String> supplierNames);

    /**
     * 根据联系人id查询供应商联系人信息
     * @Author Luo_WG
     * @Date 2023/4/17 11:00
     * @param supplierContactId supplierContactId
     * @return com.erp.model.scm.entity.SupplierEntity
     **/
    @PostMapping("feign/purchaseOrder/getSupplierContactById")
    SupplierContactEntity getSupplierContactById(@RequestBody String supplierContactId);

    /**
     * @description: klop[ - -    * @author Will
     * @date: 2023/8/4 12:09
     * @param supplierContactIds
     * @return List<SupplierContactEntity>
     */
    @PostMapping("feign/purchaseOrder/listSupplierContactByIds")
    List<SupplierContactEntity> listSupplierContactByIds(@RequestBody List<String> supplierContactIds);

    /**
     * 根据采购订单id获取到
     * 采购对应的信息
     * @author yl
     * @date 2023-04-19 17:23
     * @param purchaseOrderId
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO
     */
    @PostMapping("feign/purchaseOrder/getByOrderId")
    PurchaseOrderDTO.GetOneDTO getByOrderId(@RequestBody String purchaseOrderId);

    /**
     * 根据采购订单id获取到
     * 采购对应的信息
     * @author yl
     * @date 2023-04-19 17:23
     * @param purchaseOrderIds
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO
     */
    @PostMapping("feign/purchaseOrder/getByOrderIds")
    List<PurchaseOrderDTO.PurchaseOrderInfoDTO> getByOrderIds(@RequestBody List<String> purchaseOrderIds);

    /**
     * @description: 根据采购订单ids查询详情信息
     * @author Will
     * @date: 2023/4/24 19:44
     * @param purchaseOrderIds
     * @return List<PurchaseOrderDetailEntity>
     */
    @PostMapping("feign/purchaseOrder/listByPurchaseOrderIds")
    List<PurchaseOrderDetailEntity>listByPurchaseOrderIds(@RequestBody List<String> purchaseOrderIds);

    /**
     * 根据采购订单id查询详情信息
     * @param  id：采购订单表id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("feign/purchaseOrder/listPurchaseOrderDetailByOrderId")
    List<PurchaseOrderDetailEntity> listPurchaseOrderDetailByOrderId(@RequestBody String id);

    /**
     * 修改采购订单明细表
     * @param  entity entity
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("feign/purchaseOrder/updatePoArrivalStatus")
    Boolean updatePoArrivalStatus(@RequestBody PurchaseOrderDetailEntity entity);

    /**
     * 批量修改采购订单明细表
     * @param  entityList entityList
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("feign/purchaseOrder/updatePurchaseOrderDetailByIdBatch")
    Boolean updatePurchaseOrderDetailByIdBatch(@RequestBody List<PurchaseOrderDetailEntity> entityList);

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("feign/scmWorkOption/getTableNum")
    List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(@RequestBody List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList);

    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/syncKingdee/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:35
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/scmWorkOption/salesDemandApprove")
    List<BatchResultDTO> salesDemandApprove(@RequestBody @Validated BaseApproveParamDTO dto);

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:35
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/scmWorkOption/purchasePriceApprove")
    List<BatchResultDTO> purchasePriceApprove(@RequestBody @Validated BaseApproveParamDTO dto);

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/scmWorkOption/purchasePriceChangeApprove")
    List<BatchResultDTO> purchasePriceChangeApprove(@RequestBody @Validated BaseApproveParamDTO dto);

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/scmWorkOption/purchaseOrderApprove")
    Boolean purchaseOrderApprove(@RequestBody @Validated ApproveOneDTO dto);

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/scmWorkOption/purchaseChangeApprove")
    List<BatchResultDTO> purchaseChangeApprove(@RequestBody @Validated BaseApproveParamDTO dto);

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/scmWorkOption/purchaseApplicationApprove")
    List<BatchResultDTO> purchaseApplicationApprove(@RequestBody @Validated BaseApproveParamDTO dto);

    /**
     * 根据来源id集合获取到下推数据
     * @author yl
     * @date 2023-05-29 16:43
     * @param soIds
     * @return java.lang.Integer
     */
    @PostMapping("feign/purchaseOrder/getPushDownBySourceIds")
    Integer getPushDownBySourceIds(List<String> soIds);
    /**
     * 根据销售订单ids查询采购申请
     * @author will
     * @date 2025/6/5 14:29
     * @param soIds
     * @return List<PurchaseApplicationEntity>
     */
    @PostMapping("feign/purchaseApplication/listBySourceIds")
    List<PurchaseApplicationEntity> listPurchaseApplicationBySourceIds(List<String> soIds);

    /**
     * 根据采购订单编号获取采购订单信息
     * @param codes
     * @return
     */
    @PostMapping("feign/purchaseOrder/getPurchaseOrderByCodes")
    List<PurchaseOrderEntity> getPurchaseOrderByCodes(@RequestBody List<String> codes);
    /**
     * @description: 查询委外订单所有子级SKU生成的采购订单信息
     * @author Will
     * @date: 2023/6/15 17:46
     * @param parentPodIds
     * @return List<SubcontractOrderChildDTO>
     */
    @PostMapping("feign/purchaseOrder/listPoRefSubChildByParentPodIds")
    List<PurchaseOrderDTO.SubcontractOrderChildDTO> listPoRefSubChildByParentPodIds(@RequestBody List<String> parentPodIds);

    /**
     * @description: 根据来源id查询采购订单数据
     * @author Will
     * @date: 2023/8/7 11:04
     * @param sourceIds
     * @return List<PurchaseOrderEntity>
     */
    @PostMapping("feign/purchaseOrder/listPoBySourceIds")
    List<PurchaseOrderEntity> listPoBySourceIds(@RequestBody List<String> sourceIds);

    /**
     * 根据来源ids查询采购明细
     */
    @PostMapping("feign/purchaseOrder/listPodBySourceDetailIds")
    List<PurchaseOrderDetailEntity> listPodBySourceDetailIds(@RequestBody List<String> sourceDetailIds);

    /**
     * 根据ids查询委外订单明细
     */
    @PostMapping("feign/subcontractOrder/listSubcontractDetailByIds")
    List<SubcontractOrderDetailEntity> listSubcontractDetailByIds(@RequestBody List<String> sourceDetailIds);

    /**
     * 根据ids查询子级委外订单明细
     */
    @PostMapping("feign/subcontractOrder/listChildSubcontractDetailByIds")
    List<SubcontractOrderDetailEntity> listChildSubcontractDetailByIds(@RequestBody List<String> parentIdList);

    /**
     * 根据主表id集合查询委外订单明细
     */
    @PostMapping("feign/subcontractOrder/listSubcontractDetailByMainIds")
    List<SubcontractOrderDetailEntity> listSubcontractDetailByMainIds(@RequestBody List<String> mainIdList);

    /**
     * 根据ids查询委外订单
     */
    @PostMapping("feign/subcontractOrder/listSubcontractOrderByIds")
    List<SubcontractOrderEntity> listSubcontractOrderByIds(@RequestBody List<String> sourceIdList);

    /**
     * 根据供应商Ids查询最新的sku价格信息
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("feign/purchasePrice/listSupplierSkuPrice")
    List<PurchasePriceDTO.SupplierSkuPrice> listSupplierSkuPrice(@RequestBody List<String> ids);

    /**
     * 根据供应商Ids查询所有sku价格信息
     * @author Will
     * @date: 2023/10/27 10:06
     * @param ids
     * @return List<SupplierSkuPrice>
     */
    @PostMapping("feign/purchasePrice/listAllSupplierSkuPrice")
    List<PurchasePriceDTO.SupplierSkuPrice> listAllSupplierSkuPrice(@RequestBody List<String> ids);

    /**
     * @param skuIds
     * @return List<PurchaseOrderDetailEntity>
     * @description: 根据sku id获取审核通过的最新的采购订单(采购日期倒序)
     * @author zhangchunlin
     * @date: 2023/6/26 10:20
     * @param skuIds
     * @return List<PurchaseOrderDetailEntity>
     */
    @PostMapping("/feign/purchaseOrder/getLatest")
    List<PurchaseOrderDetailEntity> getLatest(@RequestBody List<String> skuIds);

    /**
     * @description: 根据sku id获取审核通过的最新的采购订单(创建时间倒序)
     * @author zhangchunlin
     * @date: 2023/6/26 10:20
     * @param skuIds
     * @return List<PurchaseOrderDetailEntity>
     */
    @PostMapping("/feign/purchaseOrder/getLatestByCrtTime")
    List<PurchaseOrderDetailEntity> getLatestByCrtTime(@RequestBody List<String> skuIds);

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/7/12 12:55
     * @param dto
     * @return void
     **/
    @PostMapping("feign/subcontractOrder/subcontractOrderApprove")
    Boolean subcontractOrderApprove(@RequestBody ApproveOneDTO dto);

    /**
     * @description: 新增采购订单
     * @author Will
     * @date: 2023/8/4 14:04
     * @param addDTO
     * @return Boolean
     */
    @PostMapping("feign/purchaseOrder/addPurchaseOrder")
    String addPurchaseOrder(@RequestBody PurchaseOrderDTO.AddDTO addDTO);

    /**
     * 根据采购日期查询采购采购单
     * @Author Luo_WG
     * @Date 2023/9/13 18:21
     * @param queryPurchaseDTO
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @PostMapping("feign/purchaseOrder/listPurchaseOrderByPurchaseDate")
    List<SkuCostDTO> listPurchaseOrderByPurchaseDate(@RequestBody SkuCostDTO.QueryPurchaseDTO queryPurchaseDTO);


   /**
    * @description: 根据skuId集合查询采购订单成本数据
    * @author Will
    * @date: 2023/11/23 16:23
    * @param paramDTO
    * @return List<SkuCostDTO>
    */
    @PostMapping("feign/purchaseOrder/listPurchaseOrderCost")
    List<SkuCostDTO> listPurchaseOrderCost(@RequestBody SkuCostDTO.ParamDTO paramDTO);

    /**
     * 根据bom sku id 查询委外的数据
     * @author yl
     * @date 2023-10-12 10:05
     * @param bomSkuId
     * @return java.util.List<com.erp.model.scm.dto.SubcontractOrderDTO.ListDTO>
     */
    @PostMapping("feign/subcontractOrder/listByBomSku")
    List<SubcontractOrderDTO.ListDTO> listByBomSku(@RequestBody String bomSkuId);

    /**
     * @description: 查询数据发送同步任务
     * @author Will
     * @date: 2023/10/30 11:41
     * @param syncParamDTO
     */
    @PostMapping("/feign/scmSyncTask/findDataSendSyncTask")
    void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);


    /**
     * 获取付款条件
     * @return
     */
    @GetMapping("/feign/kingdeePaymentCondition/listPaymentCondition")
    List<BaseDropDownDTO.DisabledDTO>  listPaymentCondition();
    /**
     * 批量获取列表采购单价
     * @author zdy
     * @date: 2023/10/27 10:06
     * @param list
     * @return List<PurchasePriceDTO.PriceDTO>
     */
    @PostMapping("feign/purchasePrice/batchGetPurchasePrice")
    List<PurchasePriceDTO.PriceDTO> batchGetPurchasePrice(@RequestBody List<PurchasePriceDTO.PriceDTO> list);

    /**
     * 根据sku获取采购组织列表
     * @param querySkuDTO
     * @return
     */
    @PostMapping("feign/purchasePrice/getBySkuIdList")
    public List<PurchaseSkuOrgRefEntity> getBySkuIdList(@RequestBody @Validated PurchaseSkuOrgRefDTO.QuerySkuDTO querySkuDTO);
    /**
     * @return
     */
    @PostMapping("feign/cfgSupplierSales/listAll")
    List<CfgSupplierSalesDTO.ListAllDTO> listAll();

    /**
     *
     * 根据供应商ids 查询已审核的采购订单中的sku
     */
    @PostMapping("feign/purchaseOrder/listSkuBySupplierIds")
    List<PurchaseOrderDTO.SupplierSkuDTO> listSkuBySupplierIds(@RequestBody List<String> supplierIds);

    /**
     * 获取所有供应商
     * @param
     * @return
     */
    @PostMapping("feign/purchaseOrder/listSupplier")
    List<SupplierEntity> listSupplier();

    /**
     * 获取所有供应商联系人
     * @param
     * @return
     */
    @PostMapping("feign/purchaseOrder/listSupplierContact")
    List<SupplierContactEntity> listSupplierContact();

    /**
     * 获取所有供应商账户信息
     * @param
     * @return
     */
    @PostMapping("feign/purchaseOrder/listSupplierAccount")
    List<SupplierAccountEntity> listSupplierAccount();

    /**
     * 开模通知单审核
     * @param dto
     * @return
     */
    @PostMapping("feign/scmWorkOption/assetNoticeApprove")
    List<BatchResultDTO> assetNoticeApprove(@RequestBody @Validated BaseApproveParamDTO  dto);

    /**
     * 模具采购单审核
     * @param dto
     * @return
     */
    @PostMapping("feign/scmWorkOption/assetPurchaseOrderApprove")
    List<BatchResultDTO> assetPurchaseOrderApprove(@RequestBody @Validated BaseApproveParamDTO dto);

    /**
     * 模具采购变更单审核
     * @param dto
     * @return
     */
    @PostMapping("feign/scmWorkOption/assetPurchaseChangeApprove")
    List<BatchResultDTO> assetPurchaseChangeApprove(@RequestBody @Validated BaseApproveParamDTO dto);
}
