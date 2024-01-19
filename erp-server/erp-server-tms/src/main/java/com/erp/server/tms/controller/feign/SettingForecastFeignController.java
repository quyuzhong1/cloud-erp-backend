package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.SettingForecastEntity;
import com.erp.server.tms.service.SettingForecastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lambda
 * @Classname SettingForecastFeignController
 * @Description
 * @Date 2024-01-19 17:38
 * @Created by yl
 */
@Slf4j
@RestController
@LogSystemModule("预报设置feign接口")
@RequestMapping("/feign/settingForecast")
public class SettingForecastFeignController {


    @Resource
    private SettingForecastService settingForecastService;


    
    /**
     * 根据渠道id 获取到 有效的设置
     * @description
     * @param logisticsChannelId
     * @author Lambda
     * @return 
     * @create 2024-01-19 17:48
     */
    @GetMapping("getByLogisticsChannelId")
    public  SettingForecastDTO.ForecastStatusDto getByLogisticsChannelId(@RequestParam("logisticsChannelId") String logisticsChannelId) {
         return settingForecastService.getByLogisticsChannelId(logisticsChannelId);
    }


}
