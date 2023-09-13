package com.erp.server.bi.controller.api;


import com.erp.server.bi.service.BiTargetCategorySettingService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;

/**
 * 分类 目标设置表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@RequestMapping("/biTargetCategorySetting")
public class BiTargetCategorySettingController extends BaseController {

    @Resource
    private BiTargetCategorySettingService biTargetCategorySettingService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated BiTargetCategorySettingDTO.AddDTO dto) {
        return success(biTargetCategorySettingService.add(dto));
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
        menuCode = "dmp:biTargetCategorySetting:update",
        serviceClass = BiTargetCategorySettingService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetCategorySettingDTO.UpdateDTO dto) {
        biTargetCategorySettingService.update(dto);
        return success();
    }



}
