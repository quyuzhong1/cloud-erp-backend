package com.erp.server.workflow.controller.api;


import com.erp.model.workflow.dto.CfgProcessExpDTO;
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
import com.erp.server.workflow.service.CfgProcessFieldMapService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;

import java.util.List;

/**
 * 流程设置字段配置
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("流程设置字段配置")
@RequestMapping("/cfgProcessFieldMap")
public class CfgProcessFieldMapController extends BaseController {

    @Resource
    private CfgProcessFieldMapService cfgProcessFieldMapService;

    /**
    * 新增
    * @author hcg
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
//    @PostMapping("/add")
//    @LogAction(value = LogActionEnum.INSERT, desc = "流程设置字段配置新增")
//    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated List<CfgProcessFieldMapDTO.AddOrUpdateDTO> dto) {
//        return success(cfgProcessFieldMapService.addOrUpdate(dto));
//    }

    /**
    * 修改
    * @author hcg
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
    */
//    @PostMapping("/update")
//    @LogAction(value = LogActionEnum.UPDATE, desc = "流程设置字段配置修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "workflow:cfgProcessFieldMap:update",
//        serviceClass = CfgProcessFieldMapService.class,
//        keyIdName = "id")
//    public ApiResult<?> update(@RequestBody @Validated CfgProcessFieldMapDTO.UpdateDTO dto) {
//        cfgProcessFieldMapService.update(dto);
//        return success();
//    }

    /**
     * 字段配置详情
     *
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:40
     * @param: ruleId
     * @return: List<CfgProcessFieldMapDTO.ViewDTO>
     **/
    @GetMapping("/view")
    public ApiResult<List<CfgProcessFieldMapDTO.ViewDTO>> view(@RequestParam(value = "ruleId") String ruleId) {
        return success(cfgProcessFieldMapService.view(ruleId));
    }
}
