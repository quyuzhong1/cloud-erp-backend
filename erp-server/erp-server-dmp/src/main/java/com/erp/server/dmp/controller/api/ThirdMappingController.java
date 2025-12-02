package com.erp.server.dmp.controller.api;


import com.common.core.anno.LogViewService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.ThirdMappingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.ThirdMappingDTO;

/**
 * 第三方系统映射关系表
 *
 * @author hyj
 * @since 2024-05-17
 */
@Slf4j
@RestController
@LogSystemModule("第三方系统映射关系表")
@RequestMapping("/thirdMapping")
public class ThirdMappingController extends BaseController {

    @Resource
    private ThirdMappingService thirdMappingService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author hyj
     * @date: 2024-05-17
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "第三方系统映射关系表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ThirdMappingDTO.AddDTO dto) {
        return success(thirdMappingService.add(dto));
    }


    /**
     * 查询详情
     *
     * @param viewParamDTO
     * @return ApiResult
     * @author hyj
     * @date: 2024-05-20
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:thirdMapping:view",
            serviceClass = ThirdMappingService.class,
            keyIdName = "id")
    public ApiResult<ThirdMappingDTO.MappingViewDTO> view(@RequestBody @Validated ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        return success(thirdMappingService.view(viewParamDTO));
    }


}
