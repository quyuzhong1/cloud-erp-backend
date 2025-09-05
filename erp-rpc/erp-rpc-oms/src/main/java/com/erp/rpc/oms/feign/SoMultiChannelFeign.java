package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "erp-oms", contextId = "soMultiChannelFeign",configuration = {FeignErrorDecoder.class})
public interface SoMultiChannelFeign {

    /**
     * 更新多渠道订单创建状态
     * @param createResultDTO
     */
    @PostMapping("/feign/soMultiChannel/updateSoMultiChannel")
    void updateSoMultiChannel(@RequestBody SoMultiChannelDTO.CreateResultDTO createResultDTO);

    /**
     * 根据发货单编号查询多渠道订单
     * @param deliveryCode
     * @return
     */
    @GetMapping("/feign/soMultiChannel/getByDeliveryCode")
    SoMultiChannelEntity getByDeliveryCode(@RequestParam("deliveryCode") String deliveryCode);

    /**
     * 根据发货单编号查询销售出库单生成DTO
     * @param deliveryCode
     * @return
     */
    @GetMapping("/feign/soMultiChannel/getSoOutstockGenerateB2cDTO")
    SoOutstockDTO.GenerateB2cDTO getSoOutstockGenerateB2cDTO(@RequestParam("deliveryCode") String deliveryCode);
    /**
     * 更新销售出库单标识
     * @param dto
     */
    @PostMapping("/feign/soMultiChannel/updateSoOutstock")
    void updateSoOutstock(@RequestBody PlatformSoOutStockDTO dto);
}
