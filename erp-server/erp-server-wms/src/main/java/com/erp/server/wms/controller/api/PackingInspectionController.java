package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.PackingInspectionDTO;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import com.erp.server.wms.service.PackingInspectionService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * b2c发货单包装验货
 */
@Slf4j
@RestController
@LogSystemModule("b2c发货单包装验货")
@RequestMapping("/packingInspection")
public class PackingInspectionController extends BaseController {

    @Resource
    private PackingInspectionService packingInspectionService;
    /**
     * 扫描
     * @param dto
     * @return ApiResult
     * @author lrp
     * @date: 2023-12-13
     */
    @PostMapping("/scan")
    @LogAction(value = LogActionEnum.UPDATE, desc = "包装验货扫描")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:packingInspection:scan",
            serviceClass = SoB2cDeliveryService.class,
            keyIdName = "ids")
    public ApiResult<PackingInspectionDTO.ViewDTO> scan(@RequestBody @Validated PackingInspectionDTO.ScanDTO dto) {
        PackingInspectionDTO.ViewDTO list = packingInspectionService.scan(dto);
        return success(list);
    }

    /**
     * 重置
     * @param id
     * @return ApiResult
     * @author lrp
     * @date: 2023-12-13
     */
    @GetMapping("/reset")
    @LogAction(value = LogActionEnum.UPDATE, desc = "重置")
    public ApiResult<?> reset(@RequestParam(value = "id") String id) {
        packingInspectionService.reset(id);
        return success();
    }
}
