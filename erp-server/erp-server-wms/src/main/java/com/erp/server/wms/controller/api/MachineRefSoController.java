package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.MachineRefSoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.MachineRefSoDTO;

import javax.annotation.Resource;

/**
 * 加工单和销售订单关联表
 *
 * @author will
 * @since 2023-12-06
 */
@Slf4j
@RestController
@LogSystemModule("加工单和销售订单关联表")
@RequestMapping("/machineRefSo")
public class MachineRefSoController extends BaseController {

    @Resource
    private MachineRefSoService machineRefSoService;

    /**
    * 新增
    * @author will
    * @date:  2023-12-06
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "加工单和销售订单关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated MachineRefSoDTO.AddDTO dto) {
        return success(machineRefSoService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2023-12-06
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:machineRefSo:update",
        serviceClass = MachineRefSoService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated MachineRefSoDTO.UpdateDTO dto) {
        machineRefSoService.update(dto);
        return success();
    }



}
