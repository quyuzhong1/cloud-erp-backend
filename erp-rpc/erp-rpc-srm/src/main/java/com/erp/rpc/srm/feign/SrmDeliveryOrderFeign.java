package com.erp.rpc.srm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-srm", contextId = "deliveryOrderFeign",configuration = {FeignErrorDecoder.class})
public interface SrmDeliveryOrderFeign {

    @PostMapping("/feign/deliveryOrder/tabList")
    List<DeliveryOrderDTO.TabListDTO> tabList(@RequestBody DeliveryOrderDTO.ParamDTO paramDTO);

    @GetMapping("/feign/deliveryOrder/view")
    DeliveryOrderDTO.ViewDTO view(@RequestParam("id") String id);

    @PostMapping("/feign/deliveryOrder/paging")
    PagingVO<DeliveryOrderDTO.ListDTO> paging(@RequestBody PagingDTO<DeliveryOrderDTO.ParamDTO> dto);

    @PostMapping("/feign/deliveryOrder/print")
    List<DeliveryOrderDTO.PrintDTO> print(@RequestBody BaseIdsDTO.IdsDTO dto);

    @PostMapping("/feign/deliveryOrder/confirmPrint")
    Boolean confirmPrint(@RequestBody BaseIdsDTO.IdsDTO dto);

    @PostMapping("/feign/deliveryOrder/cancelPrint")
    List<BatchResultDTO> cancelPrint(@RequestBody @Validated BaseIdsDTO.IdsDTO dto);

    @PostMapping("/feign/deliveryOrder/listDetailByDetailSourceIds")
    List<DeliveryOrderDetailEntity> listDetailByDetailSourceIds(@RequestBody List<String> purchaseDetailIds);
    @PostMapping("/feign/deliveryOrder/listDetailDTOByDetailSourceIds")
    public List<DeliveryOrderDetailDTO.ListDTO> listDetailDTOByDetailSourceIds(@RequestBody List<String> purchaseDetailIds);
    @PostMapping("/feign/deliveryOrder/getExportList")
    List<DeliveryOrderExportExcelDTO> getExportList(@RequestBody DeliveryOrderDTO.ParamDTO dto);

    @PostMapping("/feign/deliveryOrder/listGenerateReceive")
    List<DeliveryOrderDTO.GenerateReceiveListDTO> listGenerateReceive(@RequestBody BaseIdsDTO.IdsDTO dto);

    @PostMapping("/feign/deliveryOrder/pagingTotal")
    DeliveryOrderDTO.TotalInfo pagingTotal(@RequestBody DeliveryOrderDTO.ParamDTO dto);

    @PostMapping("/feign/deliveryOrder/listByIds")
    List<DeliveryOrderEntity> listByIds(@RequestBody List<String> ids);

    @PostMapping("/feign/deliveryOrder/listDetailByIds")
    List<DeliveryOrderDetailEntity> listDetailByIds(@RequestBody List<String> detailIds);

    @PostMapping("/feign/deliveryOrder/updateDeliveryOrder")
    Boolean updateDeliveryOrder(@RequestBody DeliveryOrderEntity deliveryOrderEntity);

    @PostMapping("/feign/deliveryOrder/updateDeliveryDetail")
    Boolean updateDeliveryDetail(@RequestBody List<DeliveryOrderDetailEntity> detailEntityGroupList);

    @PostMapping("/feign/deliveryOrder/confirmReceiveStatus")
    Boolean confirmReceiveStatus(@RequestBody List<String> detailIds);

    @PostMapping("/feign/deliveryOrder/unConfirmReceiveStatus")
    Boolean unConfirmReceiveStatus(@RequestBody List<String> detailIds);

    @PostMapping("/feign/deliveryOrder/cancelReceive")
    Boolean cancelReceive(@RequestBody List<String> detailIds);

    @PostMapping("/feign/deliveryOrder/exportSupplierDeliveryOrder")
    PagingVO<DeliveryOrderExportExcelDTO> exportSupplierDeliveryOrder(@RequestBody PagingDTO<DeliveryOrderDTO.ParamDTO> dto);
}
