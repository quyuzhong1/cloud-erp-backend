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
import com.erp.server.dmp.service.CfgConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.CfgConditionDTO;

import java.util.List;

/**
 * 条件配置表
 *
 * @author lrp
 * @since 2025-01-20
 */
@Slf4j
@RestController
@LogSystemModule("条件配置表")
@RequestMapping("/cfgCondition")
public class CfgConditionController extends BaseController {

    @Resource
    private CfgConditionService cfgConditionService;

    /**
    * 新增
    * @author lrp
    * @date:  2025-01-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "条件配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgConditionDTO.AddDTO dto) {
        return success(cfgConditionService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2025-01-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "条件配置表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:cfgCondition:update",
        serviceClass = CfgConditionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgConditionDTO.UpdateDTO dto) {
        cfgConditionService.update(dto);
        return success();
    }

    /**
     * 汉化管理的条件下拉
     *
     * @param
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.CfConditionDTO.CommonDTO>>
     * @author yl
     * @date 2023-10-08 14:38
     */
    @GetMapping("/listPromptWorkCondition")
    public ApiResult<List<CfgConditionDTO.ListDTO>> listPromptWorkCondition() {
        List<CfgConditionDTO.ListDTO> result = cfgConditionService.listPromptWorkCondition();
        return success(result);
    }

}
