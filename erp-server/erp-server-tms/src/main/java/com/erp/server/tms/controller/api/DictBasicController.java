package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.DictBasicService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.DictBasicDTO;

/**
 * 字典表
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("字典表")
@RequestMapping("/dictBasic")
public class DictBasicController extends BaseController {

    @Autowired
    private DictBasicService dictBasicService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "字典表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DictBasicDTO.AddDTO dto) {
        return success(dictBasicService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:dictBasic:update",
        serviceClass = DictBasicService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated DictBasicDTO.UpdateDTO dto) {
        dictBasicService.update(dto);
        return success();
    }



}
