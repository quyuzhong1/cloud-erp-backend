package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.LogisticsThirdChannelRefService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;

/**
 * 物流-第三方渠道关系表
 *
 * @author zdy
 * @since 2025-05-29
 */
@Slf4j
@RestController
@LogSystemModule("物流-第三方渠道关系表")
@RequestMapping("/logisticsThirdChannelRef")
public class LogisticsThirdChannelRefController extends BaseController {

    @Resource
    private LogisticsThirdChannelRefService logisticsThirdChannelRefService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-05-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流-第三方渠道关系表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsThirdChannelRefDTO.AddDTO dto) {
        return success(logisticsThirdChannelRefService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-05-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流-第三方渠道关系表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsThirdChannelRef:update",
        serviceClass = LogisticsThirdChannelRefService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated LogisticsThirdChannelRefDTO.UpdateDTO dto) {
        logisticsThirdChannelRefService.update(dto);
        return success();
    }



}
