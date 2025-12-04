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
import com.erp.server.dmp.service.CfgDiffStrategyDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.CfgDiffStrategyDetailDTO;

/**
 * 差异策略配置明细
 *
 * @author shukai
 * @since 2025-11-11
 */
@Slf4j
@RestController
@LogSystemModule("差异策略配置明细")
@RequestMapping("/cfgDiffStrategyDetail")
public class CfgDiffStrategyDetailController extends BaseController {

    @Resource
    private CfgDiffStrategyDetailService cfgDiffStrategyDetailService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-11-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "差异策略配置明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgDiffStrategyDetailDTO.AddDTO dto) {
        return success(cfgDiffStrategyDetailService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-11-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "差异策略配置明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:cfgDiffStrategyDetail:update",
        serviceClass = CfgDiffStrategyDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgDiffStrategyDetailDTO.UpdateDTO dto) {
        cfgDiffStrategyDetailService.update(dto);
        return success();
    }



}
