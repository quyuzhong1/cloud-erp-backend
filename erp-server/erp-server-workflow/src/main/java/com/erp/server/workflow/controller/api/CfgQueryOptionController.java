package com.erp.server.workflow.controller.api;


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
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;

/**
 * 查询option配置表(数大臣单据字段)
 *
 * @author hcg
 * @since 2025-05-15
 */
@Slf4j
@RestController
@LogSystemModule("查询option配置表(数大臣单据字段)")
@RequestMapping("/cfgQueryOption")
public class CfgQueryOptionController extends BaseController {

    @Resource
    private CfgQueryOptionService cfgQueryOptionService;

    /**
    * 新增
    * @author hcg
    * @date:  2025-05-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "查询option配置表(数大臣单据字段)新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgQueryOptionDTO.AddDTO dto) {
        return success(cfgQueryOptionService.add(dto));
    }

    /**
    * 修改
    * @author hcg
    * @date:  2025-05-15
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "查询option配置表(数大臣单据字段)修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "workflow:cfgQueryOption:update",
        serviceClass = CfgQueryOptionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgQueryOptionDTO.UpdateDTO dto) {
        cfgQueryOptionService.update(dto);
        return success();
    }



}
