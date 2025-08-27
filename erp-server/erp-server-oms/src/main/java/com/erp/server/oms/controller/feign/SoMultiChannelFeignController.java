package com.erp.server.oms.controller.feign;

import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.server.oms.service.SoMultiChannelService;
import com.erp.server.oms.service.SyncTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: 推送数据
 * @author Will
 * @date: 2023/10/19 11:05
 */
@RestController
@RequestMapping("feign/soMultiChannel")
public class SoMultiChannelFeignController {

    @Resource
    private SoMultiChannelService soMultiChannelService;

    /**
     * 更新多渠道订单创建状态
     * @param createResultDTO
     */
    @PostMapping("/updateSoMultiChannel")
    public void updateSoMultiChannel(@RequestBody SoMultiChannelDTO.CreateResultDTO createResultDTO){
        soMultiChannelService.updateSoMultiChannel(createResultDTO);
    }
}
