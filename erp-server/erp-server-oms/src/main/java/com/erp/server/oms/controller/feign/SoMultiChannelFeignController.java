package com.erp.server.oms.controller.feign;

import com.common.business.dto.PlatformSoOutStockDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.dto.SoMultiChannelDetailDTO;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.server.oms.service.SoMultiChannelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
     * 更新多渠道订单渠道信息
     * @param soMultiChannelEntity
     */
    @PostMapping("/updateSoMultiChannelById")
    public void updateSoMultiChannelById(@RequestBody SoMultiChannelEntity soMultiChannelEntity){
        soMultiChannelService.updateById(soMultiChannelEntity);
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
     * 根据销售订单编号查询多渠道订单
     * @param soId
     * @return
     */
    @GetMapping("/getBySoId")
    public SoMultiChannelEntity getBySoId(@RequestParam("soId") String soId){
        return soMultiChannelService.getBySoId(soId, Boolean.FALSE);
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

    /**
     * 更新销售出库单标识
     * @param dto
     */
    @PostMapping("/updateSoOutstock")
    public void updateSoOutstock(@RequestBody PlatformSoOutStockDTO dto){
        soMultiChannelService.updateSoOutstock(dto);
    }

    /**
     * 更新多渠道订单出库数量
     * @param outstockQtyDTOList
     */
    @PostMapping("/updateSoMultiOutstockQty")
    public void updateSoMultiOutstockQty(@RequestBody List<SoMultiChannelDetailDTO.OutstockQtyDTO> outstockQtyDTOList){
        soMultiChannelService.updateSoMultiOutstockQty(outstockQtyDTOList);
    }
}
