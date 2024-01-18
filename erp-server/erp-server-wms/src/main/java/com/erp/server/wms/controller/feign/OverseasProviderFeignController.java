package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 海外仓feign
 */
@RestController
@RequestMapping("/feign/overseasProvider")
public class OverseasProviderFeignController {

    @Resource
    private OverseasProviderService overseasProviderService;


    /**
     * 根据平台编号查询平台信息
     * @Author Luo_WG
     * @Date 2024/1/18 14:35
     * @param code
     * @return com.erp.model.wms.entity.OverseasProviderEntity
     **/
    @GetMapping("/getByPlatformCode")
    public OverseasProviderEntity getByPlatformCode(@RequestParam("code") String code) {
        return overseasProviderService.getByPlatformCode(code);
    }
}
