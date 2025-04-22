package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.DmpSoDeliveryDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpSoDeliveryDetailDTO;

/**
 * 中台配货单明细表
 *
 * @author shukai
 * @since 2025-04-17
 */
@Slf4j
@RestController
@LogSystemModule("中台配货单明细表")
@RequestMapping("/dmpSoDeliveryDetail")
public class DmpSoDeliveryDetailController extends BaseController {

    @Resource
    private DmpSoDeliveryDetailService dmpSoDeliveryDetailService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-04-17
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中台配货单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpSoDeliveryDetailDTO.AddDTO dto) {
        return success(dmpSoDeliveryDetailService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-04-17
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中台配货单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpSoDeliveryDetail:update",
        serviceClass = DmpSoDeliveryDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpSoDeliveryDetailDTO.UpdateDTO dto) {
        dmpSoDeliveryDetailService.update(dto);
        return success();
    }



}
