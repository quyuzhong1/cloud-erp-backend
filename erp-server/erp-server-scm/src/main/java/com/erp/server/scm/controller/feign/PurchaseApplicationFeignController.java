package com.erp.server.scm.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.server.scm.service.PurchaseApplicationDetailService;
import com.erp.server.scm.service.PurchaseApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
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
    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    /**
     * 新增采购申请单
     * @param
     * @return 采购申请单ID
     * @date: 2024-08-28
     * @author: tanmujin
     */
    @PostMapping("/add")
    BatchResultDTO add(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto){
        String id = purchaseApplicationService.add(dto);
        PurchaseApplicationEntity entity = purchaseApplicationService.getById(id);
        return BatchResultDTO.success(entity.getId(), entity.getCode());
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

    /**
     * 创建并提交
     */
    @PostMapping("/addAndSubmit")
    BatchResultDTO addAndSubmit(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto){
        String id = purchaseApplicationService.add(dto);
        purchaseApplicationService.submit(Collections.singletonList(id));
        PurchaseApplicationEntity entity = purchaseApplicationService.getById(id);
        return BatchResultDTO.success(entity.getId(), entity.getCode());
    }

    /**
     * 查询已下推的SKU及其数量
     */
    @PostMapping("/listSkuAndQty")
    List<PurchaseApplicationDetailDTO.PurchaseSkuQtyDTO> listSkuAndQty(List<String> skuIds){
        return purchaseApplicationDetailService.listSkuAndQty(skuIds);
    }
}
