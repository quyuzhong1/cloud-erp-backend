package com.erp.server.wms.controller.feign;

import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.server.wms.service.FirstMileDeliveryDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 头程发货单明细
 * @date 2024-08-31
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/firstMileDeliveryDetail")
public class FirstMileDeliveryDetailFeignController {

    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
    /**
     * 根据发货单ID查询明细
     */
    @PostMapping("/listByMainId")
    List<FirstMileDeliveryDetailEntity> listByMainId(@RequestBody List<String> mainIds){
        return firstMileDeliveryDetailService.lambdaQuery().in(FirstMileDeliveryDetailEntity::getMainId, mainIds).list();
    }
}
