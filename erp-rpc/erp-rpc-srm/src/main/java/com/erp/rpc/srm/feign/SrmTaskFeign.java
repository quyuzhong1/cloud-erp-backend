package com.erp.rpc.srm.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "erp-srm" ,configuration = {FeignErrorDecoder.class})
public interface SrmTaskFeign {
    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/srmSyncKingdee/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);
}
