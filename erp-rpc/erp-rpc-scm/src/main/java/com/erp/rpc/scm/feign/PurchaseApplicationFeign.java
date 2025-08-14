package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BatchResultDTO;
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
@FeignClient(name = "erp-scm", contextId = "purchaseApplication",configuration = {FeignErrorDecoder.class})
public interface PurchaseApplicationFeign {

    /**
     * 新增采购申请单
     * @param
     * @return 采购申请单ID
     * @date: 2024-08-28
     * @author: tanmujin
     */
    @PostMapping("/feign/purchaseApplication/add")
    BatchResultDTO add(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto);

    /**
     * 新增或审核
     * @author will
     * @date 2025/7/7 18:22
     * @param addDTO
     * @return BatchResultDTO
     */
    @PostMapping("/feign/purchaseApplication/addAndApprove")
    BatchResultDTO addAndApprove(PurchaseApplicationDTO.InsertDTO addDTO);


    /**
     * 根据来源ID查询采购申请单
     * @param sourceIds 来源单据ID
     * @return 采购申请单实体
     * @date: 2024-08-29
     * @author: tanmujin
     */
    @PostMapping("/feign/purchaseApplication/listBySourceIds")
    List<PurchaseApplicationEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 创建并提交
     */
    @PostMapping("/feign/purchaseApplication/addAndSubmit")
    BatchResultDTO addAndSubmit(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto);

    /**
     * 查询已下推的SKU及其数量
     */
    @PostMapping("/feign/purchaseApplication/listSkuAndQty")
    List<PurchaseApplicationDetailDTO.PurchaseSkuQtyDTO> listSkuAndQty(List<String> skuIds);

    /**
     * 查询入库数量
     */
    @PostMapping("/feign/purchaseApplication/listStockInQty")
    List<PurchaseApplicationDTO.ListDTO> listStockInQty(@RequestBody List<PurchaseApplicationDTO.ListDTO> purchaseList);

    @PostMapping("/feign/purchaseApplication/listByCodes")
    List<PurchaseApplicationEntity> listByCodes(@RequestBody List<String> list);

//    @PostMapping("/updateApproveStatus")
//    void updateApproveStatus(@RequestBody PurchaseApplicationEntity one, String approveStatus);

    @PostMapping ("/feign/purchaseApplication/updatePA")
    void updatePA(@RequestBody PurchaseApplicationDTO.updatePADTO updateDTO);

}
