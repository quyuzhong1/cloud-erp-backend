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
import com.erp.server.dmp.service.DmpThirdOutboundService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpThirdOutboundDTO;

/**
 * 第三方仓出库
 *
 * @author shukai
 * @since 2024-08-08
 */
@Slf4j
@RestController
@LogSystemModule("第三方仓出库")
@RequestMapping("/dmpThirdOutbound")
public class DmpThirdOutboundController extends BaseController {

    @Resource
    private DmpThirdOutboundService dmpThirdOutboundService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-08-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "第三方仓出库新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpThirdOutboundDTO.AddDTO dto) {
        return success(dmpThirdOutboundService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-08-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方仓出库修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpThirdOutbound:update",
        serviceClass = DmpThirdOutboundService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpThirdOutboundDTO.UpdateDTO dto) {
        dmpThirdOutboundService.update(dto);
        return success();
    }



}
