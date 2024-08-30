package com.erp.server.scm.controller.feign;

import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.server.scm.service.PurchaseApplicationService;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/feign/purchaseApplication")
public class PurchaseApplicationFeignController {

    @Resource
    private PurchaseApplicationService purchaseApplicationService;

    /**
     * 新增采购申请单
     * @param
     * @return 采购申请单ID
     * @date: 2024-08-28
     * @author: tanmujin
     */
    @PostMapping("/add")
    String add(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto){
        return purchaseApplicationService.add(dto);
    }

    /**
     * 根据来源ID查询采购申请单
     * @param sourceIds 来源单据ID
     * @return 采购申请单实体
     * @date: 2024-08-29
     * @author: tanmujin
     */
    @PostMapping("/listBySourceIds")
    List<PurchaseApplicationEntity> listBySourceIds(List<String> sourceIds){
        return purchaseApplicationService.lambdaQuery().in(PurchaseApplicationEntity::getSourceId, sourceIds).list();
    }
}
