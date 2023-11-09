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
import com.erp.server.tms.service.CfgLogisticsAuthFieldService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgLogisticsAuthFieldDTO;

/**
 * 物流商授权字段配置表
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@RestController
@LogSystemModule("物流商授权字段配置表")
@RequestMapping("/cfgLogisticsAuthField")
public class CfgLogisticsAuthFieldController extends BaseController {

    @Resource
    private CfgLogisticsAuthFieldService cfgLogisticsAuthFieldService;

    /**
    * 新增
    * @author lambda
    * @date:  2023-11-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流商授权字段配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgLogisticsAuthFieldDTO.AddDTO dto) {
        return success(cfgLogisticsAuthFieldService.add(dto));
    }

    /**
    * 修改
    * @author lambda
    * @date:  2023-11-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流商授权字段配置表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:cfgLogisticsAuthField:update",
        serviceClass = CfgLogisticsAuthFieldService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated CfgLogisticsAuthFieldDTO.UpdateDTO dto) {
        cfgLogisticsAuthFieldService.update(dto);
        return success();
    }



}
