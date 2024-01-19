package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSrmDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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
    PurchaseOrderDTO.ViewDTO srmOrderView(@Param("id") String id);
}
