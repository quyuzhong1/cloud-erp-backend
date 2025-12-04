package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.CfgDiffStrategyConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.CfgDiffStrategyConditionDTO;

/**
 * 差异策略配置条件
 *
 * @author shukai
 * @since 2025-11-11
 */
@Slf4j
@RestController
@LogSystemModule("差异策略配置条件")
@RequestMapping("/cfgDiffStrategyCondition")
public class CfgDiffStrategyConditionController extends BaseController {

    @Resource
    private CfgDiffStrategyConditionService cfgDiffStrategyConditionService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-11-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "差异策略配置条件新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgDiffStrategyConditionDTO.AddDTO dto) {
        return success(cfgDiffStrategyConditionService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-11-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "差异策略配置条件修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:cfgDiffStrategyCondition:update",
        serviceClass = CfgDiffStrategyConditionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgDiffStrategyConditionDTO.UpdateDTO dto) {
        cfgDiffStrategyConditionService.update(dto);
        return success();
    }



}
