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
import com.erp.server.oms.service.SoReceiptDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoReceiptDetailDTO;

/**
 * 收款单明细
 *
 * @author lrp
 * @since 2025-08-28
 */
@Slf4j
@RestController
@LogSystemModule("收款单明细")
@RequestMapping("/soReceiptDetail")
public class SoReceiptDetailController extends BaseController {

    @Resource
    private SoReceiptDetailService soReceiptDetailService;

    /**
    * 新增
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "收款单明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoReceiptDetailDTO.AddDTO dto) {
        return success(soReceiptDetailService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "收款单明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:soReceiptDetail:update",
        serviceClass = SoReceiptDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoReceiptDetailDTO.UpdateDTO dto) {
        soReceiptDetailService.update(dto);
        return success();
    }



}
