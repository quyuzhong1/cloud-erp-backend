package com.erp.rpc.wms.feign;

import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "firstMileDeliveryFeign")
public interface WmsFirstMileDeliveryFeign {


    /**
     * 查询已装箱并且未生成物流单的发货单
     */
    @PostMapping("/feign/firstMileDelivery/getGenerateLogisticDTO")
    List<FirstMileDeliveryDTO.GenerateLogisticDTO> getGenerateLogisticDTO(@RequestBody FirstMileDeliveryDTO.GenerateLogisticReqDTO dto);

    /**
     * 查询已装箱并且未生成物流单的发货单
     */
    @PostMapping("/feign/firstMileDelivery/updateStatus")
    Boolean updateStatus(@RequestBody FirstMileDeliveryDTO.UpdateStatusDTO dto);


    /**
     * 查询已装箱并且未生成物流单的发货单
     */
    @PostMapping("/feign/firstMileDelivery/logisticStatistics")
    List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(@RequestBody FirstMileDeliveryDTO.StatisticsReq dto);


    /**
     * 高级查询发货单
     */
    @PostMapping("/feign/firstMileDelivery/advanceQuery")
    List<FirstMileDeliveryEntity> advanceQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer);
}
