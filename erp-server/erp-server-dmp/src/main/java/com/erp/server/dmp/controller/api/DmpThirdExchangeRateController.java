package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.DmpThirdExchangeRateService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpThirdExchangeRateDTO;

/**
 * 第三方汇率
 *
 * @author shukai
 * @since 2024-08-19
 */
@Slf4j
@RestController
@LogSystemModule("第三方汇率")
@RequestMapping("/dmpThirdExchangeRate")
public class DmpThirdExchangeRateController extends BaseController {

    @Resource
    private DmpThirdExchangeRateService dmpThirdExchangeRateService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-08-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "第三方汇率新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpThirdExchangeRateDTO.AddDTO dto) {
        return success(dmpThirdExchangeRateService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-08-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方汇率修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpThirdExchangeRate:update",
        serviceClass = DmpThirdExchangeRateService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpThirdExchangeRateDTO.UpdateDTO dto) {
        dmpThirdExchangeRateService.update(dto);
        return success();
    }



}
