package com.erp.rpc.oms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
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


}
