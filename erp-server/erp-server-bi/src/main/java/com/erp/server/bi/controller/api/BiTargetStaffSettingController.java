package com.erp.server.bi.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.erp.server.bi.service.BiTargetStaffSettingService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;

/**
 * 目标管理-人员
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@LogSystemModule("目标管理")
@RequestMapping("/biTargetStaffSetting")
public class BiTargetStaffSettingController extends BaseController {

    @Resource
    private BiTargetStaffSettingService biTargetStaffSettingService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "人员目标设置添加")
    public ApiResult<String> add(@RequestBody @Validated BiTargetStaffSettingDTO.AddDTO dto) {
        return success(biTargetStaffSettingService.add(dto));
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<BiTargetStaffSettingDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        BiTargetStaffSettingDTO.ViewDTO view = biTargetStaffSettingService.view(id);
        return success(view);
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/update")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "dmp:biTargetStaffSetting:update",
//        serviceClass = BiTargetStaffSettingService.class,
//        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetStaffSettingDTO.UpdateDTO dto) {
        biTargetStaffSettingService.update(dto);
        return success();
    }


}
