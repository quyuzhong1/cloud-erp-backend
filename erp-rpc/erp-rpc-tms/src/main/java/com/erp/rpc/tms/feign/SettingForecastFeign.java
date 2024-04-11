package com.erp.rpc.tms.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Will
 * @version 1.0
 * @date 2024/4/1 12:16
 */
@FeignClient(name = "erp-tms", contextId = "settingForecast")
public interface SettingForecastFeign {


}
