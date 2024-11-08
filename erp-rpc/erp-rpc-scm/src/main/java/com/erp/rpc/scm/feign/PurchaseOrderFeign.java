package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSrmDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * 采购订单feign
 **/
@FeignClient(name = "erp-scm",contextId = "purchaseOrderFeign",configuration = {FeignErrorDecoder.class})
public interface PurchaseOrderFeign {

    /**
     * srm订单确认列表统计
     * @author zdy
     * @date: 2024/1/15 17:34
     * @return ApiResult
     */
    @PostMapping("/feign/purchaseOrder/srmOrderConfirmCount")
    List<ListStatusCountDTO.PurchaseOrderConfirmCountDTO> srmOrderConfirmCount(@RequestBody PurchaseOrderSrmDTO.SearchParamDTO dto);

    /**
     * srm订单确认分页查询
     * @author zdy
     * @date: 2024/1/15 17:34
     * @return ApiResult
     */
    @PostMapping("/feign/purchaseOrder/srmOrderConfirmPaging")
    PagingVO<PurchaseOrderDTO.ListDTO> srmOrderConfirmPaging(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto);
    /**
     * srm订单确认列表合计
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/feign/purchaseOrder/srmOrderConfirmTotal")
    PurchaseOrderDTO.ListDTO srmOrderConfirmTotal(@RequestBody @Validated PurchaseOrderDTO.SrmSearchParamDTO dto);
    /**
     * srm订单确认整单处理
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/feign/purchaseOrder/srmOrderConfirmStatus")
    List<BatchResultDTO> srmOrderConfirmStatus(@RequestBody @Validated PurchaseOrderDTO.ConfirmDTO dto);

    @GetMapping("/feign/purchaseOrder/srmOrderView")
    PurchaseOrderDTO.ViewDTO srmOrderView(@RequestParam("id") String id);

    /**
     * 根据采购订单获取订单信息
     * @param orderIds
     * @return
     */
    @PostMapping("/feign/purchaseOrder/getPurchaseOrderByIds")
    List<PurchaseOrderEntity> getPurchaseOrderByIds(@RequestBody Set<String> orderIds);

    /**
     * 根据订单明细获取信息
     * @param detailIds
     * @return
     */
    @PostMapping("/feign/purchaseOrder/getPurchaseOrderDetailByIds")
    List<PurchaseOrderDetailEntity> getPurchaseOrderDetailByIds(@RequestBody List<String> detailIds);

    /**
     * 生成送货单列表
     * @param dto
     * @return
     */
    @PostMapping("/feign/purchaseOrder/generateDeliveryList")
    List<PurchaseOrderDTO.ListDTO> generateDeliveryList(@RequestBody PurchaseOrderSrmDTO.GenerateDeliveryParamDTO dto);

    /**
     * srm待发货列表统计
     * @author zdy
     * @date: 2024/1/16 17:34
     * @return ApiResult
     */
    @PostMapping("/feign/purchaseOrder/srmWaitDeliveryCount")
    List<DeliveryOrderDTO.WaitDeliveryCountDTO> srmWaitDeliveryCount(@RequestBody PurchaseOrderSrmDTO.WaitDeliveryParamDTO dto);

    /**
     * srm待发货列表合计
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/feign/purchaseOrder/srmWaitDeliveryTotal")
    PurchaseOrderDTO.ListDTO srmWaitDeliveryTotal(@RequestBody @Validated PurchaseOrderDTO.SrmSearchParamDTO dto);
    /**
     * srm待发货分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/feign/purchaseOrder/srmWaitDeliveryPaging")
    PagingVO<PurchaseOrderDTO.ListDTO> srmWaitDeliveryPaging(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto);

    /**
     *下推采购入库单弹窗显示 根据采购订单明细获取待入库信息
     * @param purchaseDetailIdList
     * @return
     */
    @PostMapping("/feign/purchaseOrder/viewGenerateStockIn")
    List<PurchaseOrderDTO.ViewGenerateStockInDTO> viewGenerateStockIn(@RequestBody @Validated List<String>  purchaseDetailIdList);

}
