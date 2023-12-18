package com.erp.rpc.oms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soB2c")
public interface SoB2cFeign {

    /**
     * 根据b2c订单id获取物流信息
     */
    @PostMapping("/feign/soB2c/listSoB2cLogisticsByMainIdList")
    List<SoB2cLogisticsEntity> listSoB2cLogisticsByMainIdList(@RequestBody List<String> mainIdList);

    /**
     * 根据订单id 获取到运费估算的参数值
     *
     * @param orderId
     * @return
     */
    @PostMapping("/feign/soB2c/getShippingCalculationByOrderId")
    SoB2cDTO.ShippingCalculationDTO getShippingCalculationByOrderId(@RequestBody String orderId);

    /**
     * 获取明细信息
     * @param soDetailIdList
     * @return
     */
    @PostMapping("/feign/soB2c/listDetailByIds")
    List<SoB2cDetailEntity> listDetailByIds(@RequestBody List<String> soDetailIdList);

    /**
     * 根据主表id查询B2C订单主表信息
     * @param soIds
     * @return
     */
    @PostMapping("/feign/soB2c/listByIds")
    List<SoB2cEntity> listByIds(@RequestBody List<String> soIds);

    /**
     * 更改销售订单已发货
     */
    @PostMapping("/feign/soB2c/orderShipped")
    SoOutstockDTO.GenerateB2cDTO orderShipped(@RequestBody String soId);
}
