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
import com.erp.server.dmp.service.DmpCfgInputConvertValueService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgInputConvertValueDTO;

/**
 * 
 *
 * @author Luo_WG
 * @since 2024-08-08
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/dmpCfgInputConvertValue")
public class DmpCfgInputConvertValueController extends BaseController {

    @Resource
    private DmpCfgInputConvertValueService dmpCfgInputConvertValueService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2024-08-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgInputConvertValueDTO.AddDTO dto) {
        return success(dmpCfgInputConvertValueService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2024-08-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgInputConvertValue:update",
        serviceClass = DmpCfgInputConvertValueService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgInputConvertValueDTO.UpdateDTO dto) {
        dmpCfgInputConvertValueService.update(dto);
        return success();
    }



}
