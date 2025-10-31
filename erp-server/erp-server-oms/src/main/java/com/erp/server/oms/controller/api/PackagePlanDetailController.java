package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.PackagePlanDetailDTO;
import com.erp.server.oms.service.PackagePlanDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 组包计划明细
 *
 * @author zdy
 * @since 2025-10-09
 */
@Slf4j
@RestController
@LogSystemModule("组包计划明细")
@RequestMapping("/packagePlanDetail")
public class PackagePlanDetailController extends BaseController {

    @Resource
    private PackagePlanDetailService packagePlanDetailService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-10-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "组包计划明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PackagePlanDetailDTO.AddDTO dto) {
        return success(packagePlanDetailService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-10-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "组包计划明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:packagePlanDetail:update",
        serviceClass = PackagePlanDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PackagePlanDetailDTO.UpdateDTO dto) {
        packagePlanDetailService.update(dto);
        return success();
    }



}
