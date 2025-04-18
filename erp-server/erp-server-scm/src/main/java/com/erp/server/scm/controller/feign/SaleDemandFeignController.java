package com.erp.server.scm.controller.feign;

import com.erp.model.scm.entity.SalesDemandEntity;
import com.erp.server.scm.service.SalesDemandService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购申请单Feign接口
 * @date 2024-08-28
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/salesDemand")
public class SaleDemandFeignController {

    @Resource
    private SalesDemandService salesDemandService;

    /**
     *
     */
    @PostMapping("/listBySourceIds")
    List<SalesDemandEntity> listBySourceIds(@RequestBody List<String> sourceIds){
        return salesDemandService.listBySourceIds(sourceIds);
    }
}
