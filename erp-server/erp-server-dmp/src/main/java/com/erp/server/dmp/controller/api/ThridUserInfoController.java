package com.erp.server.dmp.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.ThridUserInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.ThridUserInfoDTO;

/**
 * 用户表
 *
 * @author jack
 * @since 2025-04-03
 */
@Slf4j
@RestController
@LogSystemModule("用户表")
@RequestMapping("/thridUserInfo")
public class ThridUserInfoController extends BaseController {

    @Resource
    private ThridUserInfoService thridUserInfoService;

    /**
    * 新增
    * @author jack
    * @date:  2025-04-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "用户表新增")
    public ApiResult<ThridUserInfoDTO.AddResultDTO> add(@RequestBody @Validated ThridUserInfoDTO.AddDTO dto) {
        return success(thridUserInfoService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-04-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "用户表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:thridUserInfo:update",
        serviceClass = ThridUserInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ThridUserInfoDTO.UpdateDTO dto) {
        thridUserInfoService.update(dto);
        return success();
    }



}
