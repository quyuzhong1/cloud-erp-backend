package com.erp.server.sys.controller.api;


import com.common.business.annotation.Idempotent;
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
import com.erp.server.sys.service.DictCountryOrgService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.DictCountryOrgDTO;

/**
 * 国家-组织（政治经济）关系表
 *
 * @author zdy
 * @since 2024-05-21
 */
@Slf4j
@RestController
@LogSystemModule("国家-组织（政治经济）关系表")
@RequestMapping("/dictCountryOrg")
public class DictCountryOrgController extends BaseController {

    @Resource
    private DictCountryOrgService dictCountryOrgService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-05-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "国家-组织（政治经济）关系表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DictCountryOrgDTO.AddDTO dto) {
        return success(dictCountryOrgService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-05-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "国家-组织（政治经济）关系表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:dictCountryOrg:update",
        serviceClass = DictCountryOrgService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DictCountryOrgDTO.UpdateDTO dto) {
        dictCountryOrgService.update(dto);
        return success();
    }



}
