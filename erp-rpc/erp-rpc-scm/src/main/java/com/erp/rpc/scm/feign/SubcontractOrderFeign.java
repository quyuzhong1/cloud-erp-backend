package com.erp.rpc.scm.feign;

import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 供应商采购价目表Feign
 * @date 2024-09-06
 * @author tanmujin
 */
@FeignClient(name = "erp-scm", contextId = "subcontractOrder")
public interface SubcontractOrderFeign {

    @PostMapping ("/feign/subcontractOrder/listByCodes")
    List<SubcontractOrderEntity> listByCodes(List<String> list);

    @PostMapping( "/feign/subcontractOrder/updateApproveStatus")
    void updateApproveStatus(SubcontractOrderDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}
