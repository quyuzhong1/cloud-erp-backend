package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.server.wms.service.VirtualInventoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 虚拟库存
 * @author will
 * @date 2024/6/13 16:31
 */
@RestController
@RequestMapping("feign/virtualInventory")
public class VirtualInventoryFeignController {

    @Resource
    private VirtualInventoryService virtualInventoryService;


    /**
     * 查询虚拟仓库存
     * @author will
     * @date 2024/6/13 16:33
     * @param paramDTO
     * @return List<VirtualInventoryQtyDTO>
     */
    @PostMapping("/listInventoryQty")
    public List<VirtualInventoryDTO.VirtualInventoryQtyDTO> listInventoryQty(@RequestBody @Valid VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO){
        return virtualInventoryService.listInventoryQty(paramDTO);
    }
}

