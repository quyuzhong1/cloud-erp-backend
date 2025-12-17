package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.ShopAuthDTO;
import com.erp.server.oms.service.ShopAuthService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺管理
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/shopAuth")
@LogSystemModule("店铺授权")
public class ShopAuthController extends BaseController {

    @Resource
    private ShopAuthService shopAuthService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<String> add(@RequestBody @Validated ShopAuthDTO.AddDTO dto) {
        return success(shopAuthService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:shopAuth:update",
        serviceClass = ShopAuthService.class,
        keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    public ApiResult update(@RequestBody @Validated ShopAuthDTO.UpdateDTO dto) {
        shopAuthService.update(dto);
        return success();
    }
}
