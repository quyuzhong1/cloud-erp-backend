package com.erp.rpc.wms.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 采购申请单Feign
 * @date 2024-08-28
 * @author tanmujin
 */
@FeignClient(name = "erp-scm", contextId = "purchaseApplication")
@RequestMapping("/feign/purchaseApplication")
public interface PurchaseApplicationFeign {

    /**
     * 新增采购申请单
     * @param
     * @return 采购申请单ID
     * @date: 2024-08-28
     * @author: tanmujin
     */
    @PostMapping("/add")
    BatchResultDTO add(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto);

    /**
     * 根据来源ID查询采购申请单
     * @param sourceIds 来源单据ID
     * @return 采购申请单实体
     * @date: 2024-08-29
     * @author: tanmujin
     */
    @PostMapping("/listBySourceIds")
    List<PurchaseApplicationEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 创建并提交
     */
    @PostMapping("/addAndSubmit")
    BatchResultDTO addAndSubmit(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto);

    /**
     * 查询已下推的SKU及其数量
     */
    @PostMapping("/listSkuAndQty")
    List<PurchaseApplicationDetailDTO.PurchaseSkuQtyDTO> listSkuAndQty(List<String> skuIds);
}
