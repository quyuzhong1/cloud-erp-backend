package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.ShopAuthService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.ShopAuthDTO;

/**
 * 店铺管理
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/shopAuth")
public class ShopAuthController extends BaseController {

    @Autowired
    private ShopAuthService shopAuthService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
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
    public ApiResult update(@RequestBody @Validated ShopAuthDTO.UpdateDTO dto) {
        shopAuthService.update(dto);
        return success();
    }

    /**
     * 获取虾皮授权链接
     *
     * @return
     */
    @GetMapping("/getShopeeAuthUrl")
    public ApiResult getShopeeAuthUrl(@SpringQueryMap String id) {
        return success(shopAuthService.getShopeeCodeUrl(id));
    }

    /**
     * 获取虾皮授权链接
     *
     * @return
     */
    @GetMapping("/getProductAll")
    public ApiResult getProductAll() {
        shopAuthService.getProductAll();
        return success();
    }
}
