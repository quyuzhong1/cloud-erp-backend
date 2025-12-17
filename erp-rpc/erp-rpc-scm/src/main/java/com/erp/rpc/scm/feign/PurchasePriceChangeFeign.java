package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 供应商采购价目表Feign
 * @date 2024-09-06
 * @author tanmujin
 */
@FeignClient(name = "erp-scm", contextId = "purchasePriceChange",configuration = {FeignErrorDecoder.class})
public interface PurchasePriceChangeFeign {

    @PostMapping ("/feign/purchasePriceChange/listByCodes")
    List<PurchasePriceChangeEntity> listByCodes(List<String> list);

    @PostMapping( "/feign/purchasePriceChange/updateApproveStatus")
    void updateApproveStatus(PurchasePriceChangeDTO.UpdateApprovalStatusDTO  updateApprovalStatusDTO);
}
