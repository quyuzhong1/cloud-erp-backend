package com.erp.server.sys.controller.api;


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
import com.erp.server.sys.service.ImlDictCityService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.ImlDictCityDTO;

/**
 * 城市字典表
 *
 * @author lrp
 * @since 2023-11-22
 */
@Slf4j
@RestController
@LogSystemModule("城市字典表")
@RequestMapping("/imlDictCity")
public class ImlDictCityController extends BaseController {

    @Resource
    private ImlDictCityService imlDictCityService;

    /**
    * 新增
    * @author lrp
    * @date:  2023-11-22
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "城市字典表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ImlDictCityDTO.AddDTO dto) {
        return success(imlDictCityService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2023-11-22
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "城市字典表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:imlDictCity:update",
        serviceClass = ImlDictCityService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ImlDictCityDTO.UpdateDTO dto) {
        imlDictCityService.update(dto);
        return success();
    }



}
