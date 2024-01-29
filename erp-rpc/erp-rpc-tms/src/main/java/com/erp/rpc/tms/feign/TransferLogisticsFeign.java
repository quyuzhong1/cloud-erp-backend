package com.erp.rpc.tms.feign;


import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 中转报关服务商
 */
@FeignClient(name = "erp-tms", contextId = "transferLogistics")
public interface TransferLogisticsFeign {

    @PostMapping("/feign/transferLogistics/updateDisabledBySupplierId")
    Boolean updateDisabledBySupplierId(@RequestBody TransferLogisticsSupplierDTO.UpdateDisabledDTO updateDisabledDTO);
}
