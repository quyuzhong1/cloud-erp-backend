package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.server.tms.service.TransferLogisticsSupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("中转报关物流商feign接口")
@RequestMapping("/feign/transferLogistics")
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

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @param supplierId
     * @return
     */
    @PostMapping("/listBySupplierId")
    public List<BaseIdDTO.CodeDTO> listBySupplierId(@RequestBody String supplierId) {
        return transferLogisticsSupplierService.listBySupplierId(supplierId);
    }
}
