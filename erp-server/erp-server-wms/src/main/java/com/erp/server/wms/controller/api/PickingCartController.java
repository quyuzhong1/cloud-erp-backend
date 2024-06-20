package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.PickingCartDTO;
import com.erp.server.wms.service.PickingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 拣货车管理
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@RestController
@LogSystemModule("拣货车管理")
@RequestMapping("/pickingCart")
public class PickingCartController extends BaseController {

    @Resource
    private PickingCartService pickingCartService;

    /**
    * 新增
    * @author will
    * @date:  2024-06-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "拣货车管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PickingCartDTO.AddDTO dto) {
        return success(pickingCartService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-06-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拣货车管理修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:pickingCart:update",
        serviceClass = PickingCartService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PickingCartDTO.UpdateDTO dto) {
        pickingCartService.update(dto);
        return success();
    }



}
