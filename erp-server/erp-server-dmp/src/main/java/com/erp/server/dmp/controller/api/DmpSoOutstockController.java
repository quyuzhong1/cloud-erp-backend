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
import com.erp.server.dmp.service.DmpSoOutstockService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpSoOutstockDTO;

/**
 * 中台销售订单出库详情
 *
 * @author shukai
 * @since 2024-06-26
 */
@Slf4j
@RestController
@LogSystemModule("中台销售订单出库详情")
@RequestMapping("/dmpSoOutstock")
public class DmpSoOutstockController extends BaseController {

    @Resource
    private DmpSoOutstockService dmpSoOutstockService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-26
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中台销售订单出库详情新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpSoOutstockDTO.AddDTO dto) {
        return success(dmpSoOutstockService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-26
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中台销售订单出库详情修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpSoOutstock:update",
        serviceClass = DmpSoOutstockService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpSoOutstockDTO.UpdateDTO dto) {
        dmpSoOutstockService.update(dto);
        return success();
    }



}
