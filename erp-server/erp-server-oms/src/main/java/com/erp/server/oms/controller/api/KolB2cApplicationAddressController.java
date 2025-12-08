package com.erp.server.oms.controller.api;


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
import com.erp.server.oms.service.KolB2cApplicationAddressService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KolB2cApplicationAddressDTO;

/**
 * B2C寄样申请单地址信息
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@RestController
@LogSystemModule("B2C寄样申请单地址信息")
@RequestMapping("/kolB2cApplicationAddress")
public class KolB2cApplicationAddressController extends BaseController {

    @Resource
    private KolB2cApplicationAddressService kolB2cApplicationAddressService;

    /**
    * 新增
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2C寄样申请单地址信息新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolB2cApplicationAddressDTO.AddDTO dto) {
        return success(kolB2cApplicationAddressService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2C寄样申请单地址信息修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolB2cApplicationAddress:update",
        serviceClass = KolB2cApplicationAddressService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolB2cApplicationAddressDTO.UpdateDTO dto) {
        kolB2cApplicationAddressService.update(dto);
        return success();
    }



}
