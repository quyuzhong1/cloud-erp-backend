package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsChannelBlacklistService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsChannelBlacklistDTO;

/**
 * 渠道黑名单表
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("渠道黑名单表")
@RequestMapping("/logisticsChannelBlacklist")
public class LogisticsChannelBlacklistController extends BaseController {

    @Autowired
    private LogisticsChannelBlacklistService logisticsChannelBlacklistService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "渠道黑名单表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsChannelBlacklistDTO.AddDTO dto) {
        return success(logisticsChannelBlacklistService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsChannelBlacklist:update",
        serviceClass = LogisticsChannelBlacklistService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsChannelBlacklistDTO.UpdateDTO dto) {
        logisticsChannelBlacklistService.update(dto);
        return success();
    }



}
