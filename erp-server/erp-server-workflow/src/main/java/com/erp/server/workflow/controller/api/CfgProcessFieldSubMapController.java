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
import com.erp.server.workflow.service.CfgProcessFieldSubMapService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgProcessFieldSubMapDTO;

/**
 * fieldList 明细字段映射
 *
 * @author hcg
 * @since 2025-05-14
 */
@Slf4j
@RestController
@LogSystemModule("fieldList 明细字段映射")
@RequestMapping("/cfgProcessFieldSubMap")
public class CfgProcessFieldSubMapController extends BaseController {

    @Resource
    private CfgProcessFieldSubMapService cfgProcessFieldSubMapService;

    /**
    * 新增
    * @author hcg
    * @date:  2025-05-14
    * @param dto
    * @return ApiResult<String>
    */
//    @PostMapping("/add")
//    @LogAction(value = LogActionEnum.INSERT, desc = "fieldList 明细字段映射新增")
//    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgProcessFieldSubMapDTO.AddOrUpdateDTO dto) {
//        return success(cfgProcessFieldSubMapService.addOrUpdate(dto));
//    }

    /**
    * 修改
    * @author hcg
    * @date:  2025-05-14
    * @param dto
    * @return ApiResult
    */

}
