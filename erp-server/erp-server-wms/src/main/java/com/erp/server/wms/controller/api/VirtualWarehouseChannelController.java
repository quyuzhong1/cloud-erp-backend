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
import com.erp.server.wms.service.VirtualWarehouseChannelService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;

import java.util.List;

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
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author hyj
     * @date: 2024-06-02
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓渠道新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualWarehouseChannelDTO.AddDTO dto) {
        return success(virtualWarehouseChannelService.add(dto));
    }

    /**
     * 新增
     *
     * @param batchAddDTO
     * @return ApiResult<String>
     * @author hyj
     * @date: 2024-06-02
     */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓渠道新增")
    public ApiResult<BaseResultDTO.AddDTO> batchAdd(@RequestBody @Validated VirtualWarehouseChannelDTO.BatchAddDTO batchAddDTO) {
        return success(virtualWarehouseChannelService.batchAdd(batchAddDTO));
    }


    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
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
    public ApiResult update(@RequestBody @Validated VirtualWarehouseChannelDTO.UpdateDTO dto) {
        virtualWarehouseChannelService.update(dto);
        return success();
    }


}
