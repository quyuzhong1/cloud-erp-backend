package com.erp.rpc.tms.feign;

import com.erp.model.tms.dto.SettingForecastDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Lambda
 * @Classname ForecastFeign
 * @Description TODO
 * @Date 2024-01-19 17:16
 * @Created by yl
 */
@FeignClient(name = "erp-tms", contextId = "forecast")
public interface ForecastFeign {


    /**
     * 根据渠道id 获取到对应的有效时间的预报设置信息
     * @param logisticsChannelId
     * @return
     */
    @GetMapping("/feign/settingForecast/getByLogisticsChannelId")
    SettingForecastDTO.ForecastStatusDTO getByLogisticsChannelId(@RequestParam("logisticsChannelId") String  logisticsChannelId);


}
