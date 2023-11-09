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
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;

/**
 * 物流单明细表
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@RestController
@LogSystemModule("物流单明细表")
@RequestMapping("/logisticsBillDetail")
public class LogisticsBillDetailController extends BaseController {

    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    /**
    * 新增
    * @author lambda
    * @date:  2023-11-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsBillDetailDTO.AddDTO dto) {
        return success(logisticsBillDetailService.add(dto));
    }

    /**
    * 修改
    * @author lambda
    * @date:  2023-11-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsBillDetail:update",
        serviceClass = LogisticsBillDetailService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsBillDetailDTO.UpdateDTO dto) {
        logisticsBillDetailService.update(dto);
        return success();
    }



}
