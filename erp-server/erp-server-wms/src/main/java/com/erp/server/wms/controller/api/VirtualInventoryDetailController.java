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
import com.erp.server.wms.service.VirtualInventoryDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;

/**
 * 虚拟仓库明细
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓库明细")
@RequestMapping("/virtualInventoryDetail")
public class VirtualInventoryDetailController extends BaseController {

    @Resource
    private VirtualInventoryDetailService virtualInventoryDetailService;

    /**
    * 新增
    * @author will
    * @date:  2024-12-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓库明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualInventoryDetailDTO.AddDTO dto) {
        return success(virtualInventoryDetailService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-12-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓库明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:virtualInventoryDetail:update",
        serviceClass = VirtualInventoryDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualInventoryDetailDTO.UpdateDTO dto) {
        virtualInventoryDetailService.update(dto);
        return success();
    }



}
