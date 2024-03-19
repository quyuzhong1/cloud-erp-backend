package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;

/**
 * b2c报关对账单明细
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@RestController
@LogSystemModule("b2c报关对账单明细")
@RequestMapping("/tmsB2cDeclareReconciliationDetail")
public class TmsB2cDeclareReconciliationDetailController extends BaseController {

    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;

    /**
    * 新增
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "b2c报关对账单明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsB2cDeclareReconciliationDetailDTO.AddDTO dto) {
        return success(tmsB2cDeclareReconciliationDetailService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "b2c报关对账单明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsB2cDeclareReconciliationDetail:update",
        serviceClass = TmsB2cDeclareReconciliationDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsB2cDeclareReconciliationDetailDTO.UpdateDTO dto) {
        tmsB2cDeclareReconciliationDetailService.update(dto);
        return success();
    }



}
