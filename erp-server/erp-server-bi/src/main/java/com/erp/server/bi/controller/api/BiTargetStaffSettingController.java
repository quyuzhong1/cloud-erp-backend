package com.erp.server.bi.controller.api;


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
 * 人员目标设置表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@RequestMapping("/biTargetStaffSetting")
public class BiTargetStaffSettingController extends BaseController {

    @Resource
    private BiTargetStaffSettingService biTargetStaffSettingService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated BiTargetStaffSettingDTO.AddDTO dto) {
        return success(biTargetStaffSettingService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:biTargetStaffSetting:update",
        serviceClass = BiTargetStaffSettingService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetStaffSettingDTO.UpdateDTO dto) {
        biTargetStaffSettingService.update(dto);
        return success();
    }



}
