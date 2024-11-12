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
import com.erp.server.dmp.service.DmpLogisticsTrackRegisterService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpLogisticsTrackRegisterDTO;

/**
 * 物流注册表
 *
 * @author zdy
 * @since 2024-11-12
 */
@Slf4j
@RestController
@LogSystemModule("物流注册表")
@RequestMapping("/trackRegister")
public class DmpLogisticsTrackRegisterController extends BaseController {

    @Resource
    private DmpLogisticsTrackRegisterService dmpLogisticsTrackRegisterService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-11-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流注册表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpLogisticsTrackRegisterDTO.AddDTO dto) {
        return success(dmpLogisticsTrackRegisterService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-11-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流注册表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpLogisticsTrackRegister:update",
        serviceClass = DmpLogisticsTrackRegisterService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpLogisticsTrackRegisterDTO.UpdateDTO dto) {
        dmpLogisticsTrackRegisterService.update(dto);
        return success();
    }



}
