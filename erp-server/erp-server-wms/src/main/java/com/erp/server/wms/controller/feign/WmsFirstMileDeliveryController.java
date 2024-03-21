package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.server.wms.service.FirstMileDeliveryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 头程发货单feign
 **/
@RestController
@RequestMapping("feign/firstMileDelivery")
public class WmsFirstMileDeliveryController {


    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    /**
     * 根据入参查询单据数量
     **/
    @PostMapping("/getGenerateLogisticDTO")
    public List<FirstMileDeliveryDTO.GenerateLogisticDTO> getGenerateLogisticDTO(@RequestBody FirstMileDeliveryDTO.GenerateLogisticReqDTO dto) {
        return firstMileDeliveryService.getGenerateLogisticDTO(dto);
    }

    /**
     * 更新状态
     **/
    @PostMapping("/updateStatus")
    public Boolean updateStatus(@RequestBody FirstMileDeliveryDTO.UpdateStatusDTO dto) {
        return firstMileDeliveryService.updateStatus(dto);
    }

}
