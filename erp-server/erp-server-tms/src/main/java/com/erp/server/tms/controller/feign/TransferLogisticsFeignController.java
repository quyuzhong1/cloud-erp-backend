package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.server.tms.service.TransferLogisticsSupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@LogSystemModule("中转报关物流商feign接口")
@RequestMapping("/feign/transferDeclare")
public class TransferLogisticsFeignController {
    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;

    /**
     * 更改中转物流商启用状态
     * @param dto
     * @return
     */
    @PostMapping("/updateDisabledBySupplierId")
    public Boolean updateDisabledBySupplierId(@RequestBody TransferLogisticsSupplierDTO.UpdateDisabledDTO dto){
        return transferLogisticsSupplierService.updateDisabledBySupplierId(dto);
    }

}
