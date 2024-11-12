package com.erp.server.mrp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CfgPlatformMappingDTO;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 平台映射表
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
    * 新增
    * @author will
    * @date:  2024-08-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "平台映射表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgPlatformMappingDTO.AddDTO dto) {
        return success(cfgPlatformMappingService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "平台映射表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:cfgPlatformMapping:update",
        serviceClass = CfgPlatformMappingService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgPlatformMappingDTO.UpdateDTO dto) {
        cfgPlatformMappingService.update(dto);
        return success();
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
