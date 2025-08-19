package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.SupplierEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 供应商采购价目表Feign
 * @date 2024-09-06
 * @author tanmujin
 */
@FeignClient(name = "erp-scm", contextId = "purchasePrice",configuration = {FeignErrorDecoder.class})
public interface PurchasePriceFeign {

    @PostMapping ("/feign/purchasePrice/listByCodes")
    List<PurchasePriceEntity> listByCodes(List<String> list);

    @PostMapping( "/feign/purchasePrice/updateApproveStatus")
    void updateApproveStatus(PurchasePriceDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}
