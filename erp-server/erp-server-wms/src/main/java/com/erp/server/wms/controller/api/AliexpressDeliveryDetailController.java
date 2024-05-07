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
import com.erp.server.wms.service.AliexpressDeliveryDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;

/**
 * 速卖通发货单详情
 *
 * @author lrp
 * @since 2024-05-06
 */
@Slf4j
@RestController
@LogSystemModule("速卖通发货单详情")
@RequestMapping("/aliexpressDeliveryDetail")
public class AliexpressDeliveryDetailController extends BaseController {

    @Resource
    private AliexpressDeliveryDetailService aliexpressDeliveryDetailService;

    /**
    * 新增
    * @author lrp
    * @date:  2024-05-06
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "速卖通发货单详情新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AliexpressDeliveryDetailDTO.AddDTO dto) {
        return success(aliexpressDeliveryDetailService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-05-06
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "速卖通发货单详情修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:aliexpressDeliveryDetail:update",
        serviceClass = AliexpressDeliveryDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AliexpressDeliveryDetailDTO.UpdateDTO dto) {
        aliexpressDeliveryDetailService.update(dto);
        return success();
    }



}
