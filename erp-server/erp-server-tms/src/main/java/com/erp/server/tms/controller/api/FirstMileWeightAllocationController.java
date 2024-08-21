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
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;

/**
 * 头程重量分摊
 *
 * @author tmj
 * @since 2024-08-20
 */
@Slf4j
@RestController
@LogSystemModule("头程重量分摊")
@RequestMapping("/firstMileWeightAllocation")
public class FirstMileWeightAllocationController extends BaseController {

    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;

    /**
    * 新增
    * @author tmj
    * @date:  2024-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "头程重量分摊新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FirstMileWeightAllocationDTO.AddDTO dto) {
        return success(firstMileWeightAllocationService.add(dto));
    }

    /**
    * 修改
    * @author tmj
    * @date:  2024-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程重量分摊修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:firstMileWeightAllocation:update",
        serviceClass = FirstMileWeightAllocationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated FirstMileWeightAllocationDTO.UpdateDTO dto) {
        firstMileWeightAllocationService.update(dto);
        return success();
    }



}
