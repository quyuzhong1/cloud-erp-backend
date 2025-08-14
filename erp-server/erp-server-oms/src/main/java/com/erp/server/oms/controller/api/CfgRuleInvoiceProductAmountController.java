package com.erp.server.oms.controller.api;


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
import com.erp.server.oms.service.CfgRuleInvoiceProductAmountService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.CfgRuleInvoiceProductAmountDTO;

/**
 * 发票产品总价计算规则
 *
 * @author zdy
 * @since 2025-07-14
 */
@Slf4j
@RestController
@LogSystemModule("发票产品总价计算规则")
@RequestMapping("/cfgRuleInvoiceProductAmount")
public class CfgRuleInvoiceProductAmountController extends BaseController {

    @Resource
    private CfgRuleInvoiceProductAmountService cfgRuleInvoiceProductAmountService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-07-14
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发票产品总价计算规则新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleInvoiceProductAmountDTO.AddDTO dto) {
        return success(cfgRuleInvoiceProductAmountService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-07-14
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发票产品总价计算规则修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:cfgRuleInvoiceProductAmount:update",
        serviceClass = CfgRuleInvoiceProductAmountService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleInvoiceProductAmountDTO.UpdateDTO dto) {
        cfgRuleInvoiceProductAmountService.update(dto);
        return success();
    }



}
