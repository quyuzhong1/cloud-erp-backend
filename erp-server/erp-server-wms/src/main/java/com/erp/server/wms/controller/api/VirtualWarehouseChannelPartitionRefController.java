package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.erp.server.wms.service.VirtualWarehouseChannelPartitionRefService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualWarehouseChannelPartitionRefDTO;

/**
 * 虚拟仓渠道分区关联表
 *
 * @author zdy
 * @since 2025-01-03
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓渠道分区关联表")
@RequestMapping("/virtualWarehouseChannelPartitionRef")
public class VirtualWarehouseChannelPartitionRefController extends BaseController {

    @Resource
    private VirtualWarehouseChannelPartitionRefService virtualWarehouseChannelPartitionRefService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-01-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓渠道分区关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualWarehouseChannelPartitionRefDTO.AddDTO dto) {
        return success(virtualWarehouseChannelPartitionRefService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-01-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓渠道分区关联表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:virtualWarehouseChannelPartitionRef:update",
        serviceClass = VirtualWarehouseChannelPartitionRefService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualWarehouseChannelPartitionRefDTO.UpdateDTO dto) {
        virtualWarehouseChannelPartitionRefService.update(dto);
        return success();
    }



}
