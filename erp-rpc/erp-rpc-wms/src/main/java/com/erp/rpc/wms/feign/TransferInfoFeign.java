package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.TransferInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * 仓库Feign
 *
 * @author Jim
 * @date 2023/11/29
 */
@FeignClient(name = "erp-wms", contextId = "transferInfo")
public interface TransferInfoFeign {

    /**
     * 保存并审核
     * @author zdy
     */
    @PostMapping("/feign/transferInfo/addAndApprove")
    String addAndApprove(@RequestBody @Validated TransferInfoDTO.AddDTO dto);
}


