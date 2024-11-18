package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.entity.MachineRefSoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    /**
     * @description: 根据销售订单id集合查询关联订单数据
     * @author Will
     * @date: 2023/12/6 16:04
     * @param soIds
     * @return List<MachineRefSoEntity>
     */
    @PostMapping("feign/machineInfo/listBySoIdList")
    List<MachineRefSoEntity> listBySoIdList(@RequestBody List<String> soIds);
}
