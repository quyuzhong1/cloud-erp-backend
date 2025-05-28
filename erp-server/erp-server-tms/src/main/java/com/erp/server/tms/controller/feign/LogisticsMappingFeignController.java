package com.erp.server.tms.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.server.tms.service.LogisticsMappingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 物流映射表
 * @author Will
 * @date: 2024/4/23 16:08
 */
@RestController
@RequestMapping("/feign/logisticsMapping")
public class LogisticsMappingFeignController extends BaseController {

    @Resource
    private LogisticsMappingService logisticsMappingService;


    /**
     * 根据物流映射参数获取物流映射表
     * @author Will
     * @date: 2024/4/23 16:09
     * @param paramDTO
     * @return CfgSettingEntity
     */
    @PostMapping("/getByLogisticsMappingParam")
    public LogisticsMappingEntity getByLogisticsMappingParam(@RequestBody @Validated LogisticsMappingDTO.SearchParamDTO paramDTO) {
        return logisticsMappingService.getByLogisticsMappingParam(paramDTO);
    }

    /**
     * 查询物流渠道映射
     */
    @PostMapping("/listByChannelIdAndType")
    public List<LogisticsMappingDTO.ViewDTO> listByChannelIdAndType(@RequestParam(value = "channelId") String channelId, @RequestParam(value = "type") String type) {
        return logisticsMappingService.listByChannelIdAndType(channelId,type);
    }
}
