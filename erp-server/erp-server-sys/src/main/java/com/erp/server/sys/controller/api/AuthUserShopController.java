package com.erp.server.sys.controller.api;


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
import com.erp.server.sys.service.AuthUserShopService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.AuthUserShopDTO;

/**
 * 用户-店铺权限
 *
 * @author zdy
 * @since 2025-02-27
 */
@Slf4j
@RestController
@LogSystemModule("用户-店铺权限")
@RequestMapping("/authUserShop")
public class AuthUserShopController extends BaseController {

    @Resource
    private AuthUserShopService authUserShopService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-02-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "用户-店铺权限新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AuthUserShopDTO.AddDTO dto) {
        return success(authUserShopService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-02-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "用户-店铺权限修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:sysUserShop:update",
        serviceClass = AuthUserShopService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AuthUserShopDTO.UpdateDTO dto) {
        authUserShopService.update(dto);
        return success();
    }

    /**
     * 同步OMS店铺权限到SYS
     * @return
     */
    @PostMapping("/initShopDataOmsToSys")
    public ApiResult<?> initShopDataOmsToSys(){
        authUserShopService.initShopDataOmsToSys();
        return success();
    }


}
