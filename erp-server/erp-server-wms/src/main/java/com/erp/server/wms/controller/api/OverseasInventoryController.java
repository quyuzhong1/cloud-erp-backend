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
import com.erp.server.wms.service.OverseasInventoryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.OverseasInventoryDTO;

/**
 * 海外仓库存
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("海外仓库存")
@RequestMapping("/overseasInventory")
public class OverseasInventoryController extends BaseController {

    @Resource
    private OverseasInventoryService overseasInventoryService;

    /**
    * 新增
    * @author Jim
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "海外仓库存新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated OverseasInventoryDTO.AddDTO dto) {
        return success(overseasInventoryService.add(dto));
    }

    /**
    * 修改
    * @author Jim
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "海外仓库存修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:overseasInventory:update",
        serviceClass = OverseasInventoryService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated OverseasInventoryDTO.UpdateDTO dto) {
        overseasInventoryService.update(dto);
        return success();
    }



}
