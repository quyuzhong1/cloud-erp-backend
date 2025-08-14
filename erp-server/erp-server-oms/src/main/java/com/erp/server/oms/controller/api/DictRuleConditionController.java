package com.erp.server.oms.controller.api;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.DictRuleConditionDTO;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.server.oms.service.DictRuleConditionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
@LogSystemModule("条件字典表")
public class DictRuleConditionController extends BaseController {

    @Resource
    private DictRuleConditionService dictRuleConditionService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-30
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
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
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "保存或者修改字典信息")
    public ApiResult<Object> saveOrUpdate(@RequestBody @Validated List<DictRuleConditionDTO.UpdateDTO> dto) {
        Boolean result = dictRuleConditionService.batchSaveOrUpdate(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    public ApiResult<Object> update(@RequestBody @Validated DictRuleConditionDTO.UpdateDTO dto) {
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
