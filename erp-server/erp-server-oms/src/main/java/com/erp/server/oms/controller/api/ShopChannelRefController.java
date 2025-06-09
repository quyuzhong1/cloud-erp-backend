package com.erp.server.oms.controller.api;


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
import com.erp.server.oms.service.ShopChannelRefService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.ShopChannelRefDTO;

/**
 * 店铺渠道关联表
 *
 * @author lrp
 * @since 2025-02-14
 */
@Slf4j
@RestController
@LogSystemModule("店铺渠道关联表")
@RequestMapping("/shopChannelRef")
public class ShopChannelRefController extends BaseController {

    @Resource
    private ShopChannelRefService shopChannelRefService;

    /**
    * 新增
    * @author lrp
    * @date:  2025-02-14
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "店铺渠道关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ShopChannelRefDTO.AddDTO dto) {
        return success(shopChannelRefService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2025-02-14
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "店铺渠道关联表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:shopChannelRef:update",
        serviceClass = ShopChannelRefService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ShopChannelRefDTO.UpdateDTO dto) {
        shopChannelRefService.update(dto);
        return success();
    }



}
