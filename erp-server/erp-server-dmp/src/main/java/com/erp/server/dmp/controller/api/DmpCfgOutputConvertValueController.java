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
import com.erp.server.dmp.service.DmpCfgOutputConvertValueService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgOutputConvertValueDTO;

/**
 * 推送字段映射值
 *
 * @author Luo_WG
 * @since 2024-08-20
 */
@Slf4j
@RestController
@LogSystemModule("推送字段映射值")
@RequestMapping("/dmpCfgOutputConvertValue")
public class DmpCfgOutputConvertValueController extends BaseController {

    @Resource
    private DmpCfgOutputConvertValueService dmpCfgOutputConvertValueService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2024-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "推送字段映射值新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgOutputConvertValueDTO.AddDTO dto) {
        return success(dmpCfgOutputConvertValueService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2024-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "推送字段映射值修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgOutputConvertValue:update",
        serviceClass = DmpCfgOutputConvertValueService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgOutputConvertValueDTO.UpdateDTO dto) {
        dmpCfgOutputConvertValueService.update(dto);
        return success();
    }



}
