package com.erp.server.oms.controller.feign;

import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.server.oms.service.SoMultiChannelService;
import com.erp.server.oms.service.SyncTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 根据发货单编号查询多渠道订单
     * @param deliveryCode
     * @return
     */
    @GetMapping("/getByDeliveryCode")
    public SoMultiChannelEntity getByDeliveryCode(@RequestParam("deliveryCode") String deliveryCode){
        return soMultiChannelService.getByDeliveryCode(deliveryCode);
    }

    /**
     * 根据发货单编号查询销售出库单生成DTO
     * @param deliveryCode
     * @return
     */
    @GetMapping("/getSoOutstockGenerateB2cDTO")
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockGenerateB2cDTO(@RequestParam("deliveryCode") String deliveryCode){
        return soMultiChannelService.getSoOutstockGenerateB2cDTO(deliveryCode);
    }
}
