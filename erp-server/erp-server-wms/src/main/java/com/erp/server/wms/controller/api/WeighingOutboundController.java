package com.erp.server.wms.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.WeightingOutboundDTO;
import com.erp.server.wms.service.WaveListService;
import com.erp.server.wms.service.WeightingOutboundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

/**
 * b2c发货单称重出库
 */
@Slf4j
@RestController
@LogSystemModule("b2c发货单称重出库")
@RequestMapping("/weighingOutbound")
public class WeighingOutboundController extends BaseController {

    @Resource
    private WeightingOutboundService weightingOutboundService;
    @Resource
    private WaveListService waveListService;
    /**
     * 扫描
     * @param dto
     * @return ApiResult
     * @author lrp
     * @date: 2023-12-13
     */
    @PostMapping("/scan")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "称重出库扫描:{businessCode}")
    public ApiResult<WeightingOutboundDTO.ViewDTO> scan(@RequestBody @Validated WeightingOutboundDTO.ScanDTO dto) {
        WeightingOutboundDTO.ViewDTO list = weightingOutboundService.scan(dto);
        //波次列表波次状态自动变更
        waveListService.waveListStatusAutoChange(list.getId());
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
    public ApiResult reset(@RequestParam(value = "id") @NotBlank(message = "ID不能为空") String id) {
        weightingOutboundService.reset(id);
        return success();
    }
}
