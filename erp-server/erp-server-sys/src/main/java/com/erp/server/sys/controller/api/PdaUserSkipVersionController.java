package com.erp.server.sys.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.PdaUserSkipVersionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.PdaUserSkipVersionDTO;

/**
 * PDA用户跳过版本升级记录表
 *
 * @author Luo_WG
 * @since 2023-09-12
 */
@Slf4j
@RestController
@LogSystemModule("PDA用户跳过版本升级记录表")
@RequestMapping("/pdaUserSkipVersion")
public class PdaUserSkipVersionController extends BaseController {

    @Resource
    private PdaUserSkipVersionService pdaUserSkipVersionService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-09-12
    * @param dto
    * @return ApiResult<String>
    */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增PDA用户跳过版本升级记录")
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated PdaUserSkipVersionDTO.AddDTO dto) {
        return success(pdaUserSkipVersionService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-09-12
    * @param dto
    * @return ApiResult
    */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "修改PDA用户跳过版本升级记录:id={id}")
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:pdaUserSkipVersion:update",
        serviceClass = PdaUserSkipVersionService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PdaUserSkipVersionDTO.UpdateDTO dto) {
        pdaUserSkipVersionService.update(dto);
        return success();
    }



}
