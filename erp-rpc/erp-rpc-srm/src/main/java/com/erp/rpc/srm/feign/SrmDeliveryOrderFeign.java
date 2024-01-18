package com.erp.rpc.srm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
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

    @PostMapping("/feign/deliveryOrder/listDetailByDetailSourceIds")
    List<DeliveryOrderDetailEntity> listDetailByDetailSourceIds(@RequestBody List<String> purchaseDetailIds);
}
