package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 海外仓feign
 */
@RestController
@RequestMapping("/feign/overseasWarehouse")
public class OverseasWarehouseController extends BaseController {

    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    /**
     * 通过状态获取入库单号
     */
    @PostMapping("/getReceiptNumbersForStatus")
    public List<String> getReceiptNumbersForStatus(@RequestBody List<String> statusList){
        if (CollectionUtils.isEmpty(statusList)) {
            return Collections.emptyList();
        }
        return overseasWarehouseInboundService.getReceiptNumbersForStatus(statusList);
    }

    /**
     * 通过仓库编号获取海外仓
     */
    @PostMapping("/getOverseasWarehouseListByPlatformCodes")
    public List<OverseasProviderWarehouseEntity> getOverseasWarehouseListByPlatformCodes(@RequestParam(value = "warehouseCodeList") List<String> warehouseCodeList, @RequestParam(value = "platform")String platform){
        if (CollectionUtils.isEmpty(warehouseCodeList)) {
            return Collections.emptyList();
        }
        return overseasProviderWarehouseService.listByPlatformWarehouseCode(warehouseCodeList,platform);
    }

    /**
     * 根据erp仓库id 集合获取到海外仓
     * @return
     */
    @PostMapping("/listByWarehouseId")
    public List<OverseasProviderWarehouseEntity> listByWarehouseId(@RequestBody List<String> warehouseIdList){
        return overseasProviderWarehouseService.listByWarehouseIds(warehouseIdList);
    }
}