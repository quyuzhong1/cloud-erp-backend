package com.erp.server.wms.controller.api;


import com.erp.server.wms.service.VirtualWarehouseService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.VirtualWarehouseChannelService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;

/**
 * 虚拟仓渠道
 *
 * @author hyj
 * @since 2024-06-02
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓渠道")
@RequestMapping("/virtualWarehouseChannel")
public class VirtualWarehouseChannelController extends BaseController {

    @Resource
    private VirtualWarehouseChannelService virtualWarehouseChannelService;

    /**
     * 修改
     *
     * @param batchUpdateDTO
     * @return ApiResult<String>
     * @author hyj
     * @date: 2024-06-02
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓渠道修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseChannel:update",
            serviceClass = VirtualWarehouseChannelService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddDTO> batchUpdate(@RequestBody @Validated VirtualWarehouseChannelDTO.BatchUpdateDTO batchUpdateDTO) {
        return success(virtualWarehouseChannelService.batchUpdate(batchUpdateDTO));
    }


    /**
     * 详情
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseChannel:view",
            serviceClass = VirtualWarehouseService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<VirtualWarehouseChannelDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        return success(virtualWarehouseChannelService.view(id));
    }

}
