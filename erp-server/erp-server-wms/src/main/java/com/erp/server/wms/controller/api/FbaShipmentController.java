package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.FbaShipmentService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.FbaShipmentDTO;

/**
 * FBI货件表
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@RestController
@LogSystemModule("FBI货件表")
@RequestMapping("/fbaShipment")
public class FbaShipmentController extends BaseController {

    @Autowired
    private FbaShipmentService fbaShipmentService;

    /**
     * 拉取货件信息
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     */
    @PostMapping("/pullShipment")
//    @LogAction(value = LogActionEnum.INSERT, desc = "拉取货件")
    public ApiResult pullShipment(@RequestBody @Validated FbaShipmentDTO.pullShipmentDTO dto) {
        Boolean flag = fbaShipmentService.pullShipment(dto);
        return flag ? success() : failure();
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:fbaShipment:update",
        serviceClass = FbaShipmentService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated FbaShipmentDTO.UpdateDTO dto) {
        fbaShipmentService.update(dto);
        return success();
    }


    /**
     * 列表查询
     * @author Luo_WG
     * @date: 2023-10-30
     * @param dto
     * @return ApiResult<PagingVO<FbaDeliveryDTO.ListDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<FbaShipmentDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
//        fbaShipmentService.paging(dto)
        return success();
    }
}
