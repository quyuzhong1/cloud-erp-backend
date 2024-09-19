package com.erp.rpc.plm.feign;

import com.common.business.feign.BaseDataFeign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(name = "erp-plm" , contextId = "pilotApplicationFeign")
public interface PilotApplicationFeign{

    @PostMapping("/feign/pilotApplication/updateDetailByPilotApplicationDetailIds")
    void updateDetailByPilotApplicationDetailIds(@RequestBody Map<String,String> map);
}
