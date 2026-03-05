package com.erp.server.dmp.controller.api;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.CfgAfterPlatformShopService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.CfgAfterPlatformShopDTO;

import java.util.List;

/**
 * 
 *
 * @author wtr
 * @since 2026-03-03
 */
@Slf4j
@RestController
@LogSystemModule("售后平台店铺配置")
@RequestMapping("/cfgAfterPlatformShop")
public class CfgAfterPlatformShopController extends BaseController {

    @Resource
    private CfgAfterPlatformShopService cfgAfterPlatformShopService;

    /**
    * 保存
    * @author wtr
    * @date:  2026-03-03
    * @param dtoList
    * @return ApiResult
    */
    @PostMapping("/save")
    @LogAction(value = LogActionEnum.UPDATE, desc = "保存")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:cfgAfterPlatformShop:update",
        serviceClass = CfgAfterPlatformShopService.class,
        keyIdName = "id")
    public ApiResult<List<CfgAfterPlatformShopDTO.SaveDTO>> save(@RequestBody @Validated List<CfgAfterPlatformShopDTO.SaveDTO> dtoList) {
        return success(cfgAfterPlatformShopService.save(dtoList));
    }


    /**
     * 详情
     * @author wtr
     * @date:  2026-03-03
     * @param
     * @return ApiResult
     */
    @GetMapping("/view")
    public ApiResult<List<CfgAfterPlatformShopDTO.ListDTO>> view() {
        return success(cfgAfterPlatformShopService.view());
    }


}
