package com.erp.rpc.srm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.srm.vo.SupplierConfigVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description: 对账单
 * @author Will
 * @date: 2024/1/25 9:58
 */
@FeignClient(name = "erp-srm", contextId = "poReconciliation",configuration = {FeignErrorDecoder.class})
public interface SrmPoReconciliationFeign {

    /**
     * @description: 新增对账明细
     * @author Will
     * @date: 2024/1/25 9:58
     * @param addList
     * @return List<SupplierConfigVO>
     */
    @PostMapping("/feign/poReconciliation/add")
    void add(@RequestBody @Validated List<PoReconciliationDetailDTO.AddDTO> addList);

}
