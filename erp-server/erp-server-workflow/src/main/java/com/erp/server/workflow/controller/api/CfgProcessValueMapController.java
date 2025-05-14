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
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.workflow.dto.CfgProcessValueMapDTO;

import java.util.List;

/**
 * 流程设置值映射
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("流程设置值映射")
@RequestMapping("/cfgProcessValueMap")
public class CfgProcessValueMapController extends BaseController {

    @Resource
    private CfgProcessValueMapService cfgProcessValueMapService;

    /**
    * 新增
    * @author hcg
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
//    @PostMapping("/add")
//    @LogAction(value = LogActionEnum.INSERT, desc = "流程设置值映射新增")
//    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated List<CfgProcessValueMapDTO.AddOrUpdateDTO> dto) {
//        return success(cfgProcessValueMapService.addOrUpdate(dto));
//    }

    /**
    * 修改
    * @author hcg
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
    */
//    @PostMapping("/update")
//    @LogAction(value = LogActionEnum.UPDATE, desc = "流程设置值映射修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "workflow:cfgProcessValueMap:update",
//        serviceClass = CfgProcessValueMapService.class,
//        keyIdName = "id")
//    public ApiResult<?> update(@RequestBody @Validated CfgProcessValueMapDTO.UpdateDTO dto) {
//        cfgProcessValueMapService.update(dto);
//        return success();
//    }

    /**
     * 值映射详情
     *
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:40
     * @param: BaseIdDTO
     * @return: CfgInvoiceSettingDTO.ViewDTO
     **/
    @GetMapping("/view")
    public ApiResult<List<CfgProcessValueMapDTO.ViewDTO>> view(@RequestParam(value = "fieldId") String fieldId) {
        return success(cfgProcessValueMapService.view(fieldId));
    }
}
