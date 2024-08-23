package com.erp.server.mrp.controller.api;


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
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;

/**
 * 备货物流（规则设置）
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@RestController
@LogSystemModule("备货物流（规则设置）")
@RequestMapping("/cfgRuleLogistics")
public class CfgRuleLogisticsController extends BaseController {

    @Resource
    private CfgRuleLogisticsService cfgRuleLogisticsService;

    /**
    * 新增
    * @author will
    * @date:  2024-08-23
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "备货物流（规则设置）新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleLogisticsDTO.AddDTO dto) {
        return success(cfgRuleLogisticsService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-23
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "备货物流（规则设置）修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:cfgRuleLogistics:update",
        serviceClass = CfgRuleLogisticsService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleLogisticsDTO.UpdateDTO dto) {
        cfgRuleLogisticsService.update(dto);
        return success();
    }



}
