package com.erp.server.scm.controller.feign;

import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.server.scm.service.PurchaseApplicationDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 
 * @date 2024-09-10
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/purchaseApplicationDetail")
public class PurchaseApplicationDetailFeignController {

    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    /**
     * 根据采购申请单ID查询明细
     * @param mainIds 单据ID
     * @return 采购申请单明细实体
     * @date: 2024-09-10
     * @author: tanmujin
     */
    @PostMapping("/listByMainIds")
    List<PurchaseApplicationDetailEntity> listByMainIds(@RequestBody List<String> mainIds){
        return purchaseApplicationDetailService.lambdaQuery().in(PurchaseApplicationDetailEntity::getPurchaseApplicationId, mainIds).list();
    }

    /**
     * 判断是否新品
     * @param skuIds
     * @return Boolean
     * @date: 2024-08-29
     * @author: tanmujin
     */
    @PostMapping("/existBySkuIds")
    Boolean existBySkuIds(@RequestBody List<String> skuIds){
        Boolean isNew = Boolean.TRUE ;
        Integer count = purchaseApplicationDetailService.lambdaQuery().in(PurchaseApplicationDetailEntity::getSkuId, skuIds).count();
        if(count > 0){
            isNew = Boolean.FALSE ;
        }
        return isNew;
    }
}
