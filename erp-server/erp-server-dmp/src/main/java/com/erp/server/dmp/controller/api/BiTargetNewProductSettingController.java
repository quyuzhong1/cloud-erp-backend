package com.erp.server.dmp.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.BiTargetNewProductSettingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.BiTargetNewProductSettingDTO;

/**
 * 新品目标设置表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@RequestMapping("/biTargetNewProductSetting")
public class BiTargetNewProductSettingController extends BaseController {

    @Resource
    private BiTargetNewProductSettingService biTargetNewProductSettingService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated BiTargetNewProductSettingDTO.AddDTO dto) {
        return success(biTargetNewProductSettingService.add(dto));
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
        menuCode = "dmp:biTargetNewProductSetting:update",
        serviceClass = BiTargetNewProductSettingService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetNewProductSettingDTO.UpdateDTO dto) {
        biTargetNewProductSettingService.update(dto);
        return success();
    }



}
