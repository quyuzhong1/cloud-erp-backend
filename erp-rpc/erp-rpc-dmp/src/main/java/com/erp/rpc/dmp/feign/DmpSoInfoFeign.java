package com.erp.rpc.dmp.feign;

import com.erp.model.wms.dto.ShudiyunB2cOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FeignClient(value = "erp-dmp", contextId = "DmpSoInfoFeign")
public interface DmpSoInfoFeign {


    @PostMapping("feign/dmp/shudiyunFieldDmpOrderHandler")
    List<ShudiyunB2cOrderDTO> shudiyunFieldDmpOrderHandler(@RequestParam("platformCode") String platformCode);
}
