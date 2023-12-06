package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "machineInfo")
public interface MachineInfoFeign {


    /**
     * @description: 加工单新增
     * @author Will
     * @date: 2023/12/6 14:13
     * @param addDTO
     * @return Boolean
     */
    @PostMapping("feign/machineInfo/addMachineInfo")
    String addMachineInfo(@RequestBody MachineInfoDTO.AddDTO addDTO);

}
