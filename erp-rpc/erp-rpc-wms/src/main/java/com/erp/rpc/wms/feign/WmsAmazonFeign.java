package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.PlatformOtherOutStockDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "amazonFeign",configuration = {FeignErrorDecoder.class})
public interface WmsAmazonFeign {



    /**
     * 直接消费销售出库单
     * @author Jim
     */
    @PostMapping("/feign/amz/soOutStock/consumer")
    ApiResult<T> consumerSoOutStock(@RequestBody PlatformSoOutStockDTO currentDTO);


    /**
     * 直接消费其他出库单
     * @author Jim
     */
    @PostMapping("/feign/amz/otherOutStock/consumer")
    ApiResult<T> consumerOtherSoOutStock(@RequestBody PlatformOtherOutStockDTO msg);


    /**
     * 批量新增未知国家仓库中心代号记录
     */
    @PostMapping("/feign/amz/CfgAmzFulfillmentCenter/batchInsert")
    ApiResult<T> addCfgAmzFulfillmentCenterList(@RequestBody List<CfgAmzFulfillmentCenterEntity> newCenterList);
}
