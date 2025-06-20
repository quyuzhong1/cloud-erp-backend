package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.server.wms.service.VirtualWarehouseChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    public ApiResult<BaseResultDTO.AddDTO> batchUpdate(@RequestBody @Validated VirtualWarehouseChannelDTO.BatchUpdateDTO batchUpdateDTO) {
        return success(virtualWarehouseChannelService.batchUpdate(batchUpdateDTO));
    }


    /**
     * 详情
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<VirtualWarehouseChannelDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        return success(virtualWarehouseChannelService.view(id));
    }

}
