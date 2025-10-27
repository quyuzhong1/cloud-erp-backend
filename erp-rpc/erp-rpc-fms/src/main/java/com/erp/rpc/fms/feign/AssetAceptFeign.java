package com.erp.rpc.fms.feign;

import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author: wtr
 * @Date: 2025/10/23 10:30
 * @Param:
 * @Return:
 * @Description:
 **/

@FeignClient(name = "erp-fms",contextId = "assetAceptFeign",configuration = {FeignErrorDecoder.class})
public interface AssetAceptFeign {


    @PostMapping("feign/assetAcept/getAcceptQty")
    Integer getAcceptQtyByDetailId(@RequestBody String detailId);

}
