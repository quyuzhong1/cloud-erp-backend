package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Lambda
 * @Classname SoB2cDeliveryFeign
 * @Description TODO
 * @Date 2023-12-18 11:37
 * @Created by yl
 */
@FeignClient(name = "erp-wms", contextId = "soB2cDeliveryFeign")
public interface SoB2cDeliveryFeign {

    /** 
     * @description 获取发货单详情
     * @param soDetailIdList B2C销售订单详情id
     * @author Lambda
     * @return 
     * @create 2023-12-18 11:39
     */
    @PostMapping("feign/soB2cDelivery/listBySoDetailIds")
    List<SoB2cDeliveryDetailEntity> listBySoDetailIds(@RequestBody List<String> soDetailIdList);
}
