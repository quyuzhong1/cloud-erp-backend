package com.erp.server.bi.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiTargetSkuSettingDTO;
import com.erp.server.bi.service.BiTargetSkuSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * sku 目标设置表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@RequestMapping("/biTargetSkuSetting")
public class BiTargetSkuSettingController extends BaseController {

    @Resource
    private BiTargetSkuSettingService biTargetSkuSettingService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated BiTargetSkuSettingDTO.AddDTO dto) {
        return success(biTargetSkuSettingService.add(dto));
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
        menuCode = "dmp:biTargetSkuSetting:update",
        serviceClass = BiTargetSkuSettingService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetSkuSettingDTO.UpdateDTO dto) {
        biTargetSkuSettingService.update(dto);
        return success();
    }



}
