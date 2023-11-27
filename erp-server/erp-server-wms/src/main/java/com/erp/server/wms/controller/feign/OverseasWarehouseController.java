package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}