package com.erp.server.mrp.controller.api;


import com.common.business.validator.ValidList;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CfgPlatformMappingDTO;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 平台（规则设置）
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@RestController
@LogSystemModule("平台映射表")
@RequestMapping("/cfgPlatformMapping")
public class CfgPlatformMappingController extends BaseController {

    @Resource
    private CfgPlatformMappingService cfgPlatformMappingService;

    /**
    * 修改
    * @author will
    * @date:  2024-08-29
    * @param updateList
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody @Validated ValidList<CfgPlatformMappingDTO.UpdateDTO> updateList) {
        cfgPlatformMappingService.update(updateList);
        return success();
    }

    /**
     * 查询详情
     * @author will
     * @date 2024/10/15 10:00
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgPlatformMappingDTO.MainViewDTO> view() {
        return success(cfgPlatformMappingService.view());
    }

    /**
     * 下拉查询
     * @author Will
     * @date: 2024/08/29 17:06
     * @param dto
     * @return ApiResult<List<ListDTO>>
     */
    @PostMapping("/selectPlatformMapping")
    public ApiResult<List<CfgPlatformMappingDTO.ListDTO>> selectPlatformMapping(@RequestBody CfgPlatformMappingDTO.SelectDTO dto) {
        List<CfgPlatformMappingDTO.ListDTO> list = cfgPlatformMappingService.selectPlatformMapping(dto);
        return success(list);
    }

}
