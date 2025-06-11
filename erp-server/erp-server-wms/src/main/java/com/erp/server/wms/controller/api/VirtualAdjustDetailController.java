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
import com.erp.server.wms.service.VirtualAdjustDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualAdjustDetailDTO;

/**
 * 虚拟仓调整单明细表
 *
 * @author zdy
 * @since 2025-06-09
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓调整单明细表")
@RequestMapping("/virtualAdjustDetail")
public class VirtualAdjustDetailController extends BaseController {

    @Resource
    private VirtualAdjustDetailService virtualAdjustDetailService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓调整单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualAdjustDetailDTO.AddDTO dto) {
        return success(virtualAdjustDetailService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-06-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓调整单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:virtualAdjustDetail:update",
        serviceClass = VirtualAdjustDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualAdjustDetailDTO.UpdateDTO dto) {
        virtualAdjustDetailService.update(dto);
        return success();
    }



}
