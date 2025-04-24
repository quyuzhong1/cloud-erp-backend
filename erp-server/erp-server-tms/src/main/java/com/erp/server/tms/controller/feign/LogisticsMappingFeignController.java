package com.erp.server.tms.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.server.tms.service.LogisticsMappingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @description: 根据物流渠道id获取物流映射表
     * @author jack
     * @date: 2024/10/09
     * @param id
     * @return List<LogisticsMappingEntity>
     */
    @PostMapping("/listDbByChannelId")
    List<LogisticsMappingEntity> listDbByChannelId(@RequestBody String id){
        return logisticsMappingService.listDbByChannelId(id);
    }
}
