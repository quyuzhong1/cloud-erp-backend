package com.erp.server.srm.controller.api;


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
import com.erp.server.srm.service.DeliveryOrderService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.srm.dto.DeliveryOrderDTO;

/**
 * 送货单
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@RestController
@LogSystemModule("送货单")
@RequestMapping("/deliveryOrder")
public class DeliveryOrderController extends BaseController {

    @Resource
    private DeliveryOrderService deliveryOrderService;

    /**
    * 新增
    * @author lrp
    * @date:  2024-01-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "送货单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DeliveryOrderDTO.AddDTO dto) {
        return success(deliveryOrderService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-01-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "送货单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "srm:deliveryOrder:update",
        serviceClass = DeliveryOrderService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DeliveryOrderDTO.UpdateDTO dto) {
        deliveryOrderService.update(dto);
        return success();
    }



}
