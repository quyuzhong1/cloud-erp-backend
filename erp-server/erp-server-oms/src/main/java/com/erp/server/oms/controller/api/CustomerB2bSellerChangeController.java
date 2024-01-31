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
import com.erp.server.oms.service.CustomerB2bSellerChangeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;

/**
 * b2b客户销售员变更单
 *
 * @author lrp
 * @since 2024-01-31
 */
@Slf4j
@RestController
@LogSystemModule("b2b客户销售员变更单")
@RequestMapping("/customerB2bSellerChange")
public class CustomerB2bSellerChangeController extends BaseController {

    @Resource
    private CustomerB2bSellerChangeService customerB2bSellerChangeService;


    /**
    * 修改
    * @author lrp
    * @date:  2024-01-31
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "b2b客户销售员变更单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:customerB2bSellerChange:update",
        serviceClass = CustomerB2bSellerChangeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CustomerB2bSellerChangeDTO.UpdateDTO dto) {
        customerB2bSellerChangeService.update(dto);
        return success();
    }



}
