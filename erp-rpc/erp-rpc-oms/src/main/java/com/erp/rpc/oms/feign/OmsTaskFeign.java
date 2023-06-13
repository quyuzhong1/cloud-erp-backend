package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "erp-oms")
public interface OmsTaskFeign {
    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/syncKingdee/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);
}
