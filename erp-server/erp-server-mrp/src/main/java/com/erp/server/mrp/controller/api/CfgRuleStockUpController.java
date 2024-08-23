package com.erp.server.mrp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 备货（规则设置）
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@RestController
@LogSystemModule("备货（规则设置）")
@RequestMapping("/cfgRuleStockUp")
public class CfgRuleStockUpController extends BaseController {

    @Resource
    private CfgRuleStockUpService cfgRuleStockUpService;

    /**
    * 新增
    * @author will
    * @date:  2024-08-23
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "备货（规则设置）新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleStockUpDTO.AddDTO dto) {
        return success(cfgRuleStockUpService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-23
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "备货（规则设置）修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:cfgRuleStockUp:update",
        serviceClass = CfgRuleStockUpService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleStockUpDTO.UpdateDTO dto) {
        cfgRuleStockUpService.update(dto);
        return success();
    }

    /**
     * 查看详情
     * @author will
     * @date 2024/8/23 17:06
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgRuleStockUpDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgRuleStockUpService.view(id));
    }
}
