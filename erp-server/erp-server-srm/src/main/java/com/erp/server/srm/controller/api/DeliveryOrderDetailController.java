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
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;

/**
 * 送货单明细
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@RestController
@LogSystemModule("送货单明细")
@RequestMapping("/deliveryOrderDetail")
public class DeliveryOrderDetailController extends BaseController {

    @Resource
    private DeliveryOrderDetailService deliveryOrderDetailService;

    /**
    * 新增
    * @author lrp
    * @date:  2024-01-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "送货单明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DeliveryOrderDetailDTO.AddDTO dto) {
        return success(deliveryOrderDetailService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-01-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "送货单明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "srm:deliveryOrderDetail:update",
        serviceClass = DeliveryOrderDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DeliveryOrderDetailDTO.UpdateDTO dto) {
        deliveryOrderDetailService.update(dto);
        return success();
    }



}
