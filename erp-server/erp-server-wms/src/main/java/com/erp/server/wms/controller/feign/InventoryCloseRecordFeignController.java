package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.inventory.InventoryClosedRecordDTO;
import com.erp.server.wms.service.InventoryClosedRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Classname: InventoryFeignController

 * @CreateTime: 2023-04-27  10:31
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping("/feign/inventoryClosedRecord")
public class InventoryCloseRecordFeignController extends BaseController {

    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;


    /**
     * 根据金蝶组织id查询关账时间
     * @author Will
     * @date: 2024/2/28 17:50
     * @param category
     * @return List<SkuInventoryTotalDTO>
     */
    @PostMapping("/mapByOrgId")
    public Map<String, LocalDate> mapByOrgId(@RequestBody String category) {
        Map<String, LocalDate> map = inventoryClosedRecordService.mapByOrgId(category);
        return map;
    }

    /**
     * 根据组织id集合验证是否关账
     * @author Will
     * @date: 2024/2/28 17:50
     * @param closedParamList
     */
    @PostMapping("/checkHsClosed")
    public void checkHsClosed(@RequestBody @Valid List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList) {
        inventoryClosedRecordService.checkHsClosed(closedParamList);
    }

}