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
import com.erp.server.oms.service.KolB2cApplicationDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KolB2cApplicationDetailDTO;

/**
 * B2C寄样申请单明细
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@RestController
@LogSystemModule("B2C寄样申请单明细")
@RequestMapping("/kolB2cApplicationDetail")
public class KolB2cApplicationDetailController extends BaseController {

    @Resource
    private KolB2cApplicationDetailService kolB2cApplicationDetailService;

    /**
    * 新增
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2C寄样申请单明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolB2cApplicationDetailDTO.AddDTO dto) {
        return success(kolB2cApplicationDetailService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2C寄样申请单明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolB2cApplicationDetail:update",
        serviceClass = KolB2cApplicationDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolB2cApplicationDetailDTO.UpdateDTO dto) {
        kolB2cApplicationDetailService.update(dto);
        return success();
    }



}
