package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.SettingForecastEntity;
import com.erp.server.tms.service.SettingForecastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
     * @param dto
     * @author Lambda
     * @return
     * @create 2024-01-19 17:48
     */
    @PostMapping("/getByLogisticsChannelId")
    public SettingForecastDTO.ForecastStatusDTO getByLogisticsChannelId(@RequestBody SettingForecastDTO.FindSettingForecastDTO dto) {
         return settingForecastService.getByLogisticsChannelId(dto);
    }

    @GetMapping("/getSettingForecastByLogisticsSupplierId")
    public SettingForecastEntity getSettingForecastByLogisticsSupplierId(@RequestParam("logisticsSupplierId") String logisticsSupplierId) {
        return settingForecastService.getSettingForecastByLogisticsSupplierId(logisticsSupplierId);
    }


    /**
     * 根据物流商 有效的设置
     * @description
     * @param dto
     * @author Lambda
     * @return
     * @create 2024-01-19 17:48
     */
    @PostMapping("/getByLogisticsSupplier")
    public SettingForecastDTO.ForecastStatusDTO getByLogisticsSupplier(@RequestBody SettingForecastDTO.FindByLogisticsSupplierDTO dto) {
        return settingForecastService.getByLogisticsSupplier(dto);
    }

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
