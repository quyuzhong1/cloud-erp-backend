package com.erp.server.dmp.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpDateDimensionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpDateDimensionDTO;

/**
 * 时间维度表
 *
 * @author zdy
 * @since 2023-12-08
 */
@Slf4j
@RestController
@LogSystemModule("时间维度表")
@RequestMapping("/dmpDateDimension")
public class DmpDateDimensionController extends BaseController {

    @Autowired
    private DmpDateDimensionService dmpDateDimensionService;

    /**
    * 新增
    * @author zdy
    * @date:  2023-12-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "时间维度表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpDateDimensionDTO.AddDTO dto) {
        return success(dmpDateDimensionService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2023-12-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpDateDimension:update",
        serviceClass = DmpDateDimensionService.class,
        keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "时间维度表修改")
    public ApiResult update(@RequestBody @Validated DmpDateDimensionDTO.UpdateDTO dto) {
        dmpDateDimensionService.update(dto);
        return success();
    }



}
