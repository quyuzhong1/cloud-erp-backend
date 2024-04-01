package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.entity.SettingForecastEntity;
import com.erp.server.tms.service.SettingForecastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("预报设置feign接口")
@RequestMapping("/feign/settingForecast")
public class SettingForecastFeignController {

    @Resource
    private SettingForecastService settingForecastService;


   /**
    * 根据物流渠道id集合查询
    * @author Will
    * @date: 2024/4/1 12:21
    * @param logisticsChannelIdList
    * @return List<SettingForecastEntity>
    */
    @PostMapping("/listByLogisticsChannelIdList")
    public List<SettingForecastEntity> listByLogisticsChannelIdList(@RequestBody List<String> logisticsChannelIdList) {
        List<SettingForecastEntity> list = settingForecastService.listByLogisticsChannelIdList(logisticsChannelIdList);
        return list;
    }
}
