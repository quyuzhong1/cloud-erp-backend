package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.CfgConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.CfConditionDTO;

import java.util.List;

/**
 * 规则下拉
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Slf4j
@RestController
@RequestMapping("/cfCondition")
public class CfgConditionController extends BaseController {

    @Autowired
    private CfgConditionService cfConditionService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-30
     */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated CfConditionDTO.AddDTO dto) {
        return success(cfConditionService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-30
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:cfCondition:update",
            serviceClass = CfgConditionService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated CfConditionDTO.UpdateDTO dto) {
        cfConditionService.update(dto);
        return success();
    }

    /**
     * 根据条件code 获取到对应逻辑关系
     *
     * @param conditionCode
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<CfConditionDTO.CommonDTO>> listByConditionCode(@RequestParam("conditionCode") String conditionCode) {
        List<CfConditionDTO.CommonDTO> resultList = cfConditionService.listByConditionCode(conditionCode);
        return success(resultList);

    }


}
