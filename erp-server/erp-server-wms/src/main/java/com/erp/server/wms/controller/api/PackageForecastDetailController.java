package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.PackageForecastDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.PackageForecastDetailDTO;

/**
 * 组包预报详情
 *
 * @author Lambda
 * @since 2024-01-26
 */
@Slf4j
@RestController
@LogSystemModule("组包预报详情")
@RequestMapping("/packageForecastDetail")
public class PackageForecastDetailController extends BaseController {

    @Resource
    private PackageForecastDetailService packageForecastDetailService;

    /**
    * 新增
    * @author Lambda
    * @date:  2024-01-26
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "组包预报详情新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PackageForecastDetailDTO.AddDTO dto) {
        return success(packageForecastDetailService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2024-01-26
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "组包预报详情修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:packageForecastDetail:update",
        serviceClass = PackageForecastDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PackageForecastDetailDTO.UpdateDTO dto) {
        packageForecastDetailService.update(dto);
        return success();
    }



}
