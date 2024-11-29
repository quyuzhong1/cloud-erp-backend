package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.RemotePostcodeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.RemotePostcodeDTO;

/**
 * 偏远邮编组
 *
 * @author jack
 * @since 2024-11-29
 */
@Slf4j
@RestController
@LogSystemModule("偏远邮编组")
@RequestMapping("/remotePostcode")
public class RemotePostcodeController extends BaseController {

    @Resource
    private RemotePostcodeService remotePostcodeService;

    /**
    * 新增
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "偏远邮编组新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated RemotePostcodeDTO.AddDTO dto) {
        return success(remotePostcodeService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "偏远邮编组修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:remotePostcode:update",
        serviceClass = RemotePostcodeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated RemotePostcodeDTO.UpdateDTO dto) {
        remotePostcodeService.update(dto);
        return success();
    }



}
