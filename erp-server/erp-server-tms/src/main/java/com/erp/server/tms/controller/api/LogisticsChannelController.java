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
import com.erp.server.tms.service.LogisticsChannelService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;

import java.util.List;

/**
 * 物流商管理
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("物流渠道")
@RequestMapping("/logisticsChannel")
public class LogisticsChannelController extends BaseController {

    @Autowired
    private LogisticsChannelService logisticsChannelService;

    /**
    * 物流渠道新增
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流渠道新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsChannelDTO.AddDTO dto) {
        return success(logisticsChannelService.add(dto));
    }

    /**
    * 物流渠道修改
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsChannel:update",
        serviceClass = LogisticsChannelService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsChannelDTO.UpdateDTO dto) {
        logisticsChannelService.update(dto);
        return success();
    }


    /**
     * 物流渠道列表
     * @author Will
     * @date: 2023/11/10 9:57
     * @return ApiResult<AddDTO>
     */
    @GetMapping("/listLogisticsChannel")
    public ApiResult<List<LogisticsChannelDTO.ListSelectDTO>> listLogisticsChannel() {
        return success(logisticsChannelService.listLogisticsChannel());
    }

}
