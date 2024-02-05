package com.erp.rpc.tms.feign;


import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 中转报关服务商
 */
@FeignClient(name = "erp-tms", contextId = "transferLogistics")
public interface TransferLogisticsFeign {

    /**
     * 更改中转物流商启用状态
     * @param updateDisabledDTO
     * @return
     */
    @PostMapping("/feign/transferLogistics/updateDisabledBySupplierId")
    Boolean updateDisabledBySupplierId(@RequestBody TransferLogisticsSupplierDTO.UpdateDisabledDTO updateDisabledDTO);

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @param supplierId
     * @return
     */
    @PostMapping("/feign/transferLogistics/listBySupplierId")
    List<BaseIdDTO.CodeDTO> listBySupplierId(@RequestBody String supplierId);
}
