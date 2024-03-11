package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SkuMappingRuleDTO;
import com.erp.server.oms.convert.SkuMappingRuleConverter;
import com.erp.server.oms.service.SkuMappingRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
        return success(SkuMappingRuleConverter.INSTANCE.entityToListDto(skuMappingRuleService.listOrderByPriorityAndUpdateTime()));
    }

    /**
     * 分页查询
     * @author lrp
     * @date:  2023-12-21
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SkuMappingRuleDTO.ListDTO>> paging(@RequestBody PagingDTO<SkuMappingRuleDTO.ParamsDTO> dto) {
        PagingVO<SkuMappingRuleDTO.ListDTO> pagingVO = skuMappingRuleService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 详情
     * @author lrp
     * @date:  2023-12-21
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<SkuMappingRuleDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        SkuMappingRuleDTO.ViewDTO viewDTO = skuMappingRuleService.view(id);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:skuMappingRule:update",
            serviceClass = SkuMappingRuleService.class,
            keyIdName = "id")
    public ApiResult<String> getSkuRuleTest(@RequestBody @Validated SkuMappingRuleDTO.RuleTestDTO dto) {
        String result = skuMappingRuleService.getSkuRuleTest(dto);
        return success(result);
    }
}
