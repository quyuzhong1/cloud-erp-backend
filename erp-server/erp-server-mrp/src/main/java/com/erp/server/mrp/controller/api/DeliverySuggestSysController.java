package com.erp.server.mrp.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.mrp.service.DeliverySuggestSysService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.mrp.dto.DeliverySuggestSysDTO;

/**
 * 建议发货变更表
 *
 * @author will
 * @since 2024-10-21
 */
@Slf4j
@RestController
@LogSystemModule("建议发货变更表")
@RequestMapping("/deliverySuggestChange")
public class DeliverySuggestSysController extends BaseController {

    @Resource
    private DeliverySuggestSysService deliverySuggestSysService;

    /**
    * 新增
    * @author will
    * @date:  2024-10-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "建议发货变更表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DeliverySuggestSysDTO.AddDTO dto) {
        return success(deliverySuggestSysService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-10-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "建议发货变更表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:deliverySuggestChange:update",
        serviceClass = DeliverySuggestSysService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DeliverySuggestSysDTO.UpdateDTO dto) {
        deliverySuggestSysService.update(dto);
        return success();
    }



}
