package com.erp.rpc.dmp.feign;


import com.common.business.config.FeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;

/**
 * DMP远程调用亚马逊SDK接口
 *
 * @author Jim
 * @since 2023-11-08
 */
@FeignClient(value = "erp-dmp", path = "feign/dmp", contextId = "DmpAmazonFeign",configuration = {FeignErrorDecoder.class})
public interface DmpAmazonFeign {

    /**
     * 拉取货件
     *
     * @Author Jim
     * @since 2023-10-10
     **/
    @PostMapping("/amazon/getShipment")
    Boolean pullShipment(@RequestBody DmpPullShipmentDTO dto);


    /**
     * 缓存和获取亚马逊授权相关信息
     *
     * @Author Jim
     * @since 2023-12-01
     **/
    @PostMapping("/amazon/shop")
    AmazonShopInfoDTO getShopAuth(@RequestBody String shopId);

    /**
     * 重推销售出库单
     *
     * @Author Jim
     * @since 2024-03-12
     **/
    @PostMapping("/amazon/checkAndSendSoOutStock")
    Boolean checkAndSendSoOutStock(@RequestBody DmpPullSoOutStockDTO dto);
}