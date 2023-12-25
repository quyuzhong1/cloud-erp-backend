package com.erp.server.oms.controller.api;


import com.erp.server.oms.convert.SkuMappingRuleConverter;
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
import com.erp.server.oms.service.SkuMappingRuleService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SkuMappingRuleDTO;

import java.util.List;

/**
 * sku对照表匹配规则
 *
 * @author lrp
 * @since 2023-12-21
 */
@Slf4j
@RestController
@LogSystemModule("sku对照表匹配规则")
@RequestMapping("/skuMappingRule")
public class SkuMappingRuleController extends BaseController {

    @Resource
    private SkuMappingRuleService skuMappingRuleService;

    /**
     * 查询
     * @author lrp
     * @date:  2023-12-21
     * @return ApiResult<String>
     */
    @GetMapping("/list")
    public ApiResult<List<SkuMappingRuleDTO.ListDTO>> list() {
        return success(SkuMappingRuleConverter.INSTANCE.entityToListDto(skuMappingRuleService.listOrderByPriority()));
    }

    /**
     * 详情
     * @author lrp
     * @date:  2023-12-21
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<SkuMappingRuleDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SkuMappingRuleDTO.ViewDTO viewDTO = skuMappingRuleService.view(dto.getId());
        return success(viewDTO);
    }

    /**
    * 新增
    * @author lrp
    * @date:  2023-12-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "sku对照表匹配规则新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SkuMappingRuleDTO.AddDTO dto) {
        return success(skuMappingRuleService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2023-12-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku对照表匹配规则修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:skuMappingRule:update",
        serviceClass = SkuMappingRuleService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SkuMappingRuleDTO.UpdateDTO dto) {
        skuMappingRuleService.update(dto);
        return success();
    }

    /**
     * 启用或禁用
     * @author lrp
     * @date:  2023-12-21
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/enableOrDisable")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku对照表匹配规则修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:skuMappingRule:update",
            serviceClass = SkuMappingRuleService.class,
            keyIdName = "id")
    public ApiResult<?> enableOrDisable(@RequestBody @Validated SkuMappingRuleDTO.StatusDTO dto) {
        skuMappingRuleService.enableOrDisable(dto);
        return success();
    }


    /**
     * 测试
     * @author lrp
     * @date:  2023-12-21
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/getSkuRuleTest")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku对照表匹配规则修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:skuMappingRule:update",
            serviceClass = SkuMappingRuleService.class,
            keyIdName = "id")
    public ApiResult<List<String>> getSkuRuleTest(@RequestBody @Validated SkuMappingRuleDTO.RuleTestDTO dto) {
        List<String> result = skuMappingRuleService.getSkuRuleTest(dto);
        return success(result);
    }
}
