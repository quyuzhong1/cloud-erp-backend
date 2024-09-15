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
import com.erp.server.wms.service.SubcontractReturnOrderDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SubcontractReturnOrderDetailDTO;

/**
 * 委外退料明细单
 *
 * @author zdy
 * @since 2024-09-15
 */
@Slf4j
@RestController
@LogSystemModule("委外退料明细单")
@RequestMapping("/subcontractReturnOrderDetail")
public class SubcontractReturnOrderDetailController extends BaseController {

    @Resource
    private SubcontractReturnOrderDetailService subcontractReturnOrderDetailService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "委外退料明细单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SubcontractReturnOrderDetailDTO.AddDTO dto) {
        return success(subcontractReturnOrderDetailService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "委外退料明细单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:subcontractReturnOrderDetail:update",
        serviceClass = SubcontractReturnOrderDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SubcontractReturnOrderDetailDTO.UpdateDTO dto) {
        subcontractReturnOrderDetailService.update(dto);
        return success();
    }



}
