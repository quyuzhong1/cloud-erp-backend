package com.erp.rpc.tms.feign;

import com.erp.model.tms.entity.SettingForecastEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @date 2024/4/1 12:16
 */
@FeignClient(name = "erp-tms", contextId = "settingForecast")
public interface SettingForecastFeign {

    /**
     * 根据物流渠道id集合查询
     * @author Will
     * @date: 2024/4/1 12:22
     * @param logisticsChannelIdList
     * @return List<SettingForecastEntity>
     */
    @PostMapping("/feign/settingForecast/listByLogisticsChannelIdList")
    List<SettingForecastEntity> listByLogisticsChannelIdList(@RequestBody List<String> logisticsChannelIdList);
}
