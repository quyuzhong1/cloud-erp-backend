package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.server.wms.service.AliexpressDeliveryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("/feign/aliexpressDelivery")
public class AliexpressDeliveryFeignController extends BaseController {

    @Resource
    private AliexpressDeliveryService aliexpressDeliveryService;

    /**
     * 新增速卖通发货单
     * @Author Luo_WG
     * @Date 2024/1/30 17:01
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/add")
    public BaseResultDTO.AddDTO add(@RequestBody AliexpressDeliveryDTO.AddDTO dto) {
        return aliexpressDeliveryService.add(dto);
    }
}
