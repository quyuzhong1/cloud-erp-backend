package com.erp.rpc.wms.feign;

import com.common.business.dto.PlatformOtherOutStockDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.core.controller.vo.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-wms", contextId = "amazonFeign")
public interface WmsAmazonFeign {



    /**
     * 直接消费销售出库单
     * @author Jim
     */
    @PostMapping("/feign/amz/soOutStock/consumer")
    ApiResult<?> consumerSoOutStock(@RequestBody PlatformSoOutStockDTO currentDTO);


    /**
     * 直接消费其他出库单
     * @author Jim
     */
    @PostMapping("/feign/amz/otherOutStock/consumer")
    ApiResult<?> consumerOtherSoOutStock(@RequestBody PlatformOtherOutStockDTO msg);
}
