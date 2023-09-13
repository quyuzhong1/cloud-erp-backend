package com.erp.server.oms.controller.api;


import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.DictRuleConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.DictRuleConditionDTO;

import java.util.List;

/**
 * 订单规则
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Slf4j
@RestController
@RequestMapping("/dictRuleCondition")
public class DictRuleConditionController extends BaseController {

    @Autowired
    private DictRuleConditionService dictRuleConditionService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-30
     */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated DictRuleConditionDTO.AddDTO dto) {
        return success(dictRuleConditionService.add(dto));
    }

    /**
     * 保存或者修改字典信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdateBatch")
    public ApiResult saveOrUpdate(@RequestBody @Validated List<DictRuleConditionDTO.UpdateDTO> dto) {
        Boolean result = dictRuleConditionService.batchSaveOrUpdate(dto);
        return result == true ? success() : failure();
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
    public ApiResult update(@RequestBody @Validated DictRuleConditionDTO.UpdateDTO dto) {
        dictRuleConditionService.update(dto);
        return success();
    }

    /**
     * 规则条件下拉
     *
     * @param type
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listByKey(@RequestParam("type") String type) {
        String field = DictBasicTypeEnum.FIELD.getType();
        if (field.equals(type)) {
            return success(dictRuleConditionService.listRuleField());
        } else {
            return success(dictRuleConditionService.listByType(type));
        }

    }


}
