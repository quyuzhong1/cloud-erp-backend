package com.erp.server.scm.controller.feign;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.*;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 采购订单feign
 *
 * @Author Luo_WG
 * @Date 2023/4/13 11:11
 **/
@RestController
@RequestMapping("feign/purchaseOrder")
public class PurchaseOrderFeignController {


    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private SupplierContactService supplierContactService;

    @Resource
    private SalesDemandService salesDemandService;

    @Resource
    private PurchaseApplicationService purchaseApplicationService;

    /**
     * 根据id查询采购订单
     *
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/getPurchaseOrderById")
    public PurchaseOrderEntity getPurchaseOrderById(@RequestBody String id) {
        return purchaseOrderService.getById(id);
    }


    /**
     * 根据ids查询采购订单
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/listPurchaseOrderByIds")
    public List<PurchaseOrderEntity> listPurchaseOrderByIds(@RequestBody List<String> ids) {
        return purchaseOrderService.listByIds(ids);
    }

    /**
     * 根据采购订单id查询供应商
     *
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/getOrderSupplierByOrderId")
    public PurchaseOrderSupplierEntity getOrderSupplierByOrderId(@RequestBody String id) {
        return purchaseOrderSupplierService.getByPurchaseOrderId(id);
    }

    /**
     * @description: 根据采购订单id集合查询供应商
     * @author Will
     * @date: 2024/1/25 10:15
     * @param idList
     * @return List<PurchaseOrderSupplierEntity>
     */
    @PostMapping("/listOrderSupplierByOrderIdList")
    public List<PurchaseOrderSupplierEntity> listOrderSupplierByOrderIdList(@RequestBody List<String> idList) {
        return purchaseOrderSupplierService.listOrderSupplierByOrderIdList(idList);
    }


    /**
     * 根据采购订单id 获取供应商仓库信息
     *
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/getByOrderId")
    public PurchaseOrderDTO.GetOneDTO getByOrderId(@RequestBody String id) {
        return purchaseOrderService.getPurchaseOrder(id);
    }


    /**
     * 根据采购订单ids 获取供应商仓库信息
     *
     * @param purchaseOrderIds：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/getByOrderIds")
    public List<PurchaseOrderDTO.PurchaseOrderInfoDTO> getByOrderId(@RequestBody List<String> purchaseOrderIds) {
        return purchaseOrderService.getPurchaseOrderByOrderIds(purchaseOrderIds);
    }

    /**
     * 根据采购订单详情id查询详情信息
     *
     * @param id：采购订单详情表id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/listPurchaseOrderDetailById")
    public List<PurchaseOrderDetailEntity> listPurchaseOrderDetailById(@RequestBody List<String> id) {
        return purchaseOrderDetailService.listDetailByIds(id);
    }

    /**
     * @param ids
     * @return List<PurchaseOrderDetailEntity>
     * @description: 根据采购订单ids查询明细
     * @author Will
     * @date: 2023/4/24 19:43
     */
    @PostMapping("/listByPurchaseOrderIds")
    public List<PurchaseOrderDetailEntity> listByPurchaseOrderIds(@RequestBody List<String> ids) {
        return purchaseOrderDetailService.listByPurchaseOrderIds(ids);
    }


    /**
     * 根据采购订单id查询详情信息
     *
     * @param id：采购订单表id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/listPurchaseOrderDetailByOrderId")
    public List<PurchaseOrderDetailEntity> listPurchaseOrderDetailByOrderId(@RequestBody String id) {
        return purchaseOrderDetailService.listPurchaseOrderDetailByOrderId(id);
    }

    /**
     * @param supplierId
     * @return SupplierEntity
     * @description: 查询供应商信息
     * @author Will
     * @date: 2023/4/14 10:10
     */
    @PostMapping("/getSupplierById")
    public SupplierEntity getSupplierById(@RequestBody String supplierId) {
        return supplierService.getById(supplierId);
    }

    /**
     * @param supplierIds
     * @return SupplierEntity
     * @description: 查询供应商信息
     * @author Will
     * @date: 2023/4/14 10:10
     */
    @PostMapping("/getSupplierByIdList")
    public List<SupplierEntity> getSupplierById(@RequestBody List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) {
            return Collections.emptyList();
        }
        return supplierService.listByIds(supplierIds);
    }

    /**
     * 根据联系人id查询供应商联系人信息
     *
     * @param supplierContactId supplierContactId
     * @return com.erp.model.scm.entity.SupplierEntity
     * @Author Luo_WG
     * @Date 2023/4/17 11:00
     **/
    @PostMapping("/getSupplierContactById")
    public SupplierContactEntity getSupplierContactById(@RequestBody String supplierContactId) {
        return supplierContactService.getById(supplierContactId);
    }

    /**
     * @description: 根据联系人ids查询供应商联系人信息
     * @author Will
     * @date: 2023/8/4 12:10
     * @param supplierContactIds
     * @return List<SupplierContactEntity>
     */
    @PostMapping("/listSupplierContactByIds")
    public List<SupplierContactEntity> listSupplierContactByIds(@RequestBody List<String> supplierContactIds) {
        if (CollectionUtils.isEmpty(supplierContactIds)) {
            return Collections.emptyList();
        }
        return supplierContactService.listByIds(supplierContactIds);
    }


    /**
     * 修改采购订单明细表
     *
     * @param entity entity
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/20 18:51
     **/
    @PostMapping("/updatePoArrivalStatus")
    public Boolean updatePoArrivalStatus(@RequestBody PurchaseOrderDetailEntity entity) {
        return purchaseOrderDetailService.updatePoArrivalStatus(entity);
    }

    /**
     * 批量修改采购订单明细表
     *
     * @param entityList entityList
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/20 18:51
     **/
    @PostMapping("/updatePurchaseOrderDetailByIdBatch")
    public Boolean updatePurchaseOrderDetailByIdBatch(@RequestBody List<PurchaseOrderDetailEntity> entityList) {
        return purchaseOrderDetailService.updateBatchById(entityList);
    }

    @PostMapping("/getPushDownBySourceIds")
    public Integer getPushDownBySoIds(@RequestBody List<String> soIds) {
        return salesDemandService.getPushDownBySourceIds(soIds);
    }

    /**
     * 根据采购订单编号获取采购订单信息
     * @param codes
     * @return
     */
    @PostMapping("/getPurchaseOrderByCodes")
    List<PurchaseOrderEntity> getPurchaseOrderByCodes(@RequestBody List<String> codes) {
        return purchaseOrderService.findByCodes(codes);
    }

    /**
     * 查询委外订单所有子级SKU生成的采购订单信息
     * @param parentPodIds
     * @return
     */
    @PostMapping("/listPoRefSubChildByParentPodIds")
    public List<PurchaseOrderDTO.SubcontractOrderChildDTO> listPoRefSubChildByParentPodIds(@RequestBody List<String> parentPodIds) {
        return purchaseOrderService.listPoRefSubChildByParentPodIds(parentPodIds);
    }

    /**
     * @description: 新增采购订单
     * @author Will
     * @date: 2023/8/4 14:07
     * @param addDTO
     * @return String
     */
    @PostMapping("/addPurchaseOrder")
    public String addPurchaseOrder(@RequestBody PurchaseOrderDTO.AddDTO addDTO) {
        PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.add(addDTO);
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return purchaseOrderEntity.getCode();
    }

    /**
     * @description: 根据来源ids查询采购明细
     * @author Will
     * @date: 2023/6/26 10:20
     * @param sourceDetailIds
     * @return List<PurchaseOrderDetailEntity>
     */
    @PostMapping("/listPodBySourceDetailIds")
    public List<PurchaseOrderDetailEntity> listPodBySourceDetailIds(@RequestBody List<String> sourceDetailIds) {
        return purchaseOrderDetailService.listBySourceDetailIds(sourceDetailIds);
    }

    /**
     * @description: 根据来源id查询采购订单数据
     * @author Will
     * @date: 2023/8/7 11:05
     * @param sourceIds
     * @return List<PurchaseOrderEntity>
     */
    @PostMapping("/listPoBySourceIds")
    public List<PurchaseOrderEntity> listPoBySourceIds(@RequestBody List<String> sourceIds) {
        return purchaseOrderService.listPoBySourceIds(sourceIds);
    }

    /**
     * @description: 根据sku id集合获取审核通过的最新的采购订单明细信息(采购日期倒序)
     * @author zhangchunlin
     * @date: 2023/6/26 10:20
     * @param skuIds
     * @return List<PurchaseOrderDetailEntity>
     */
    @PostMapping("/getLatest")
    public List<PurchaseOrderDetailEntity> getLatest(@RequestBody List<String> skuIds) {
        return purchaseOrderDetailService.getLatest(skuIds);
    }
    /**
     * @description: 根据sku id集合获取审核通过的最新的采购订单明细信息(创建时间倒序)
     * @author Will
     * @date: 2023/8/31 11:43
     * @param skuIds
     * @return List<PurchaseOrderDetailEntity>
     */
    @PostMapping("/getLatestByCrtTime")
    public List<PurchaseOrderDetailEntity> getLatestByCrtTime(@RequestBody List<String> skuIds) {
        return purchaseOrderDetailService.getLatestByCrtTime(skuIds);
    }

    /**
     * 根据采购日期查询采购采购单
     * @Author Luo_WG
     * @Date 2023/9/13 18:21
     * @param queryPurchaseDTO
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @PostMapping("/listPurchaseOrderByPurchaseDate")
    public List<SkuCostDTO> listPurchaseOrderByPurchaseDate(@RequestBody SkuCostDTO.QueryPurchaseDTO queryPurchaseDTO) {
        return purchaseOrderService.listPurchaseOrderByPurchaseDate(queryPurchaseDTO);
    }

    /**
     * @description: 根据skuId集合查询成本数据
     * @author Will
     * @date: 2023/11/23 16:24
     * @param paramDTO
     * @return List<SkuCostDTO>
     */
    @PostMapping("/listPurchaseOrderCost")
    public List<SkuCostDTO> listPurchaseOrderCost(@RequestBody SkuCostDTO.ParamDTO paramDTO) {
        return purchaseOrderService.listPurchaseOrderCost(paramDTO);
    }


    /**
     * 统计采购订单
     */
    @PostMapping("/statisticsBySupplier")
    public PurchaseStatisticsDTO.ResponseDTO statisticsBySupplier(@RequestBody PurchaseStatisticsDTO.RequestDTO requestDTO) {
        return purchaseOrderService.statisticsBySupplier(requestDTO);
    }


    /**
     * 统计采购订单
     */
    @PostMapping("/statisticsExecutionStatus")
    public PurchaseStatisticsDTO.StatusDTO statisticsExecutionStatus(@RequestBody PurchaseStatisticsDTO.RequestDTO requestDTO) {
        return purchaseOrderService.statisticsExecutionStatus(requestDTO);
    }

    /**
     * srm订单确认列表统计
     * @author zdy
     * @date: 2024/1/15 17:34
     * @return ApiResult
     */
    @PostMapping("/srmOrderConfirmCount")
    public List<ListStatusCountDTO.PurchaseOrderConfirmCountDTO> srmOrderConfirmCount(@RequestBody PurchaseOrderSrmDTO.SearchParamDTO dto) {
        return purchaseOrderService.srmOrderConfirmCount(dto);
    }

    /**
     * srm订单确认列表分页查询
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmOrderConfirmPaging")
    public PagingVO<PurchaseOrderDTO.ListDTO> srmOrderConfirmPaging(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto) {
        PagingVO<PurchaseOrderDTO.ListDTO> pagingVO = purchaseOrderService.srmOrderConfirmPaging(dto);
        return pagingVO;
    }
    /**
     * srm订单确认列表合计
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmOrderConfirmTotal")
    public PurchaseOrderDTO.ListDTO srmOrderConfirmTotal(@RequestBody @Validated PurchaseOrderDTO.SrmSearchParamDTO dto) {
        return purchaseOrderService.srmOrderConfirmTotal(dto);
    }
    /**
     * srm订单确认整单处理
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmOrderConfirmStatus")
    public List<BatchResultDTO> srmOrderConfirmStatus(@RequestBody @Validated PurchaseOrderDTO.ConfirmDTO dto) {
        List<BatchResultDTO> batchResultDTOS = purchaseOrderService.srmOrderConfirmStatus(dto);
        return batchResultDTOS;
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<PurchaseOrderDTO.viewDTO>
     */
//    @LogViewService
    @GetMapping("/srmOrderView")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "purchase_user_id",
//            menuCode = "scm:purchaseOrder:view",
//            serviceClass = PurchaseOrderService.class,
//            keyIdName = "id")
    public PurchaseOrderDTO.ViewDTO srmOrderView(@RequestParam("id") String id) {
        return purchaseOrderService.view(id);
    }

    /**
     * 根据采购订单获取订单信息
     * @param orderIds
     * @return
     */
    @PostMapping("/getPurchaseOrderByIds")
    public List<PurchaseOrderEntity> getPurchaseOrderByIds(@RequestBody Set<String> orderIds){
        if (CollectionUtils.isEmpty(orderIds)){
            return Collections.emptyList();
        }
        return purchaseOrderService.listByIds(orderIds);
    }

    /**
     * 根据订单明细获取信息
     * @param detailIds
     * @return
     */
    @PostMapping("/getPurchaseOrderDetailByIds")
    public List<PurchaseOrderDetailEntity> getPurchaseOrderDetailByIds(@RequestBody List<String> detailIds){
        if (CollectionUtils.isEmpty(detailIds)){
            return Collections.emptyList();
        }
        return purchaseOrderDetailService.listByIds(detailIds);
    }

    /**
     * 生成送货单列表
     * @param dto
     * @return
     */
    @PostMapping("/generateDeliveryList")
    public List<PurchaseOrderDTO.ListDTO> generateDeliveryList(@RequestBody PurchaseOrderSrmDTO.GenerateDeliveryParamDTO dto){
        List<PurchaseOrderDTO.ListDTO> list = purchaseOrderService.generateDeliveryList(dto);
        return list;
    }

    /**
     * srm待发货列表统计
     * @author zdy
     * @date: 2024/1/16 17:34
     * @return ApiResult
     */
    @PostMapping("/srmWaitDeliveryCount")
    public List<DeliveryOrderDTO.WaitDeliveryCountDTO> srmWaitDeliveryCount(@RequestBody PurchaseOrderSrmDTO.WaitDeliveryParamDTO dto) {
        return purchaseOrderService.srmWaitDeliveryCount(dto);
    }


    /**
     * srm待发货分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmWaitDeliveryPaging")
    public PagingVO<PurchaseOrderDTO.ListDTO> srmWaitDeliveryPaging(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto) {
        return purchaseOrderService.srmWaitDeliveryPaging(dto);
    }

    /**
     * srm待发货列表合计
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmWaitDeliveryTotal")
    public PurchaseOrderDTO.ListDTO srmWaitDeliveryTotal(@RequestBody @Validated PurchaseOrderDTO.SrmSearchParamDTO dto) {
        return purchaseOrderService.srmWaitDeliveryTotal(dto);
    }
    /**
     * 下推采购入库单弹窗显示 根据采购订单明细获取待入库信息
     * @author zdy
     * @date: 2023/4/13 11:20
     * @param purchaseDetailIdList
     * @return ApiResult<List<ViewGenerateReceiveDTO>>
     */
    @PostMapping("/viewGenerateStockIn")
    public List<PurchaseOrderDTO.ViewGenerateStockInDTO> viewGenerateStockIn(@RequestBody @Validated List<String>  purchaseDetailIdList) {
        return purchaseOrderService.viewGenerateStockIn(purchaseDetailIdList);
    }

    /**
     * 通过sku 供应商查询所有采购订单
     * @param purchaseCalcQtyParamsDTO 参数
     */
    @PostMapping("/listAllPurchaseBySkuIdAndSupplier")
    public List<PurchaseOrderDTO.PurchaseCalcQtyDTO> listAllPurchaseBySkuIdAndSupplier(@RequestBody PurchaseOrderDTO.PurchaseCalcQtyParamsDTO purchaseCalcQtyParamsDTO) {
        return purchaseOrderService.listAllPurchaseBySkuIdAndSupplier(purchaseCalcQtyParamsDTO);
    }

    /**
     * 根据id更新合同状态
     */
    @PostMapping("/updateContractStatusById")
    public void updateContractStatusById(@RequestBody PurchaseOrderDTO.ContractStampStatusParamsDTO  contractStampStatusParamsDTO) {
        purchaseOrderService.updateContractStampStatus(contractStampStatusParamsDTO);
    }

    /**
     */
    @PostMapping("/listSkuBySupplierIds")
    public List<PurchaseOrderDTO.SupplierSkuDTO> listSkuBySupplierIds(@RequestBody List<String> supplierIds) {
        return purchaseOrderService.listSkuBySupplierIds(supplierIds);
    }
}
