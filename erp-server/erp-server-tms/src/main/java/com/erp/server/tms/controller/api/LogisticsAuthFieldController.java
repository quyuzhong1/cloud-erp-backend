package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsAuthFieldDTO;

/**
 * 物流授权字段值表
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@RestController
@LogSystemModule("物流授权字段值表")
@RequestMapping("/logisticsAuthField")
public class LogisticsAuthFieldController extends BaseController {

    @Resource
    private LogisticsAuthFieldService logisticsAuthFieldService;

    /**
    * 新增
    * @author lambda
    * @date:  2023-11-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流授权字段值表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsAuthFieldDTO.AddDTO dto) {
        return success(logisticsAuthFieldService.add(dto));
    }

    /**
    * 修改
    * @author lambda
    * @date:  2023-11-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流授权字段值表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsAuthField:update",
        serviceClass = LogisticsAuthFieldService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsAuthFieldDTO.UpdateDTO dto) {
        logisticsAuthFieldService.update(dto);
        return success();
    }



}
