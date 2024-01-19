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
import com.erp.server.tms.service.TransferDeclareService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TransferDeclareDTO;

/**
 * 中转报关表
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("中转报关表")
@RequestMapping("/transferDeclare")
public class TransferDeclareController extends BaseController {

    @Resource
    private TransferDeclareService transferDeclareService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2024-01-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转报关表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TransferDeclareDTO.AddDTO dto) {
        return success(transferDeclareService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2024-01-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中转报关表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:transferDeclare:update",
        serviceClass = TransferDeclareService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TransferDeclareDTO.UpdateDTO dto) {
        transferDeclareService.update(dto);
        return success();
    }



}
