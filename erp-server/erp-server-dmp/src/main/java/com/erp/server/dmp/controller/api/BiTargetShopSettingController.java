package com.erp.server.dmp.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.BiTargetShopSettingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.BiTargetShopSettingDTO;

/**
 * 店铺目标设置表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@RequestMapping("/biTargetShopSetting")
public class BiTargetShopSettingController extends BaseController {

    @Resource
    private BiTargetShopSettingService biTargetShopSettingService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated BiTargetShopSettingDTO.AddDTO dto) {
        return success(biTargetShopSettingService.add(dto));
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
        menuCode = "dmp:biTargetShopSetting:update",
        serviceClass = BiTargetShopSettingService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetShopSettingDTO.UpdateDTO dto) {
        biTargetShopSettingService.update(dto);
        return success();
    }



}
