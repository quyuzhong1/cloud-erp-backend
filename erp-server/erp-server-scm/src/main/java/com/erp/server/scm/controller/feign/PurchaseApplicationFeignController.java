package com.erp.server.scm.controller.feign;

import com.common.business.dto.base.BatchResultDTO;
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
        PurchaseApplicationEntity entity = purchaseApplicationService.add(dto);
        return BatchResultDTO.success(entity.getId(), entity.getCode());
    }
    /**
     * 新增审核
     * @author will
     * @date 2025/7/7 18:23
     * @param dto
     * @return BatchResultDTO
     */
    @PostMapping("/addAndApprove")
    BatchResultDTO addAndApprove(@RequestBody @Validated PurchaseApplicationDTO.InsertDTO dto){
        PurchaseApplicationEntity entity = purchaseApplicationService.addAndApprove(dto);
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
    List<PurchaseApplicationEntity> listBySourceIds(@RequestBody List<String> sourceIds){
        return purchaseApplicationService.lambdaQuery().in(PurchaseApplicationEntity::getSourceId, sourceIds).list();
    }

    /**
     * 创建并提交
     */
    @PostMapping("/addAndSubmit")
    BatchResultDTO addAndSubmit(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto){
        return purchaseApplicationService.addAndSubmit(dto);
    }

    /**
     * 查询已下推的SKU及其数量
     */
    @PostMapping("/listSkuAndQty")
    List<PurchaseApplicationDetailDTO.PurchaseSkuQtyDTO> listSkuAndQty(@RequestBody List<String> sourceIds){
        return purchaseApplicationDetailService.listSkuAndQty(sourceIds,null);
    }

    /**
     * 查询入库数量
     */
    @PostMapping("/listStockInQty")
    List<PurchaseApplicationDTO.ListDTO> listStockInQty(@RequestBody List<PurchaseApplicationDTO.ListDTO> purchaseList){
        return purchaseApplicationService.listStockInQty(purchaseList);
    }

     @PostMapping("/listByCodes")
    List<PurchaseApplicationEntity> listByCodes(@RequestBody List<String> list){
        return purchaseApplicationService.listByCodes(list);
    }

    @PostMapping("/updateApproveStatus")
    void updateApproveStatus(PurchaseApplicationEntity one, String approveStatus){
         purchaseApplicationService.updateApproveStatus(one, approveStatus);
    }

     @PostMapping ("/updatePA")
     void updatePA(PurchaseApplicationDTO.updatePADTO updateDTO){
          purchaseApplicationService.updatePA(updateDTO);
     }
}
