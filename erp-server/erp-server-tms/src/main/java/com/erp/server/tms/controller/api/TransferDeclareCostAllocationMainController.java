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
import com.erp.server.tms.service.TransferDeclareCostAllocationMainService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TransferDeclareCostAllocationMainDTO;

/**
 * 中转费用分摊主表
 *
 * @author shukai
 * @since 2024-12-07
 */
@Slf4j
@RestController
@LogSystemModule("中转费用分摊主表")
@RequestMapping("/transferDeclareCostAllocationMain")
public class TransferDeclareCostAllocationMainController extends BaseController {

    @Resource
    private TransferDeclareCostAllocationMainService transferDeclareCostAllocationMainService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-12-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转费用分摊主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TransferDeclareCostAllocationMainDTO.AddDTO dto) {
        return success(transferDeclareCostAllocationMainService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-12-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中转费用分摊主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:transferDeclareCostAllocationMain:update",
        serviceClass = TransferDeclareCostAllocationMainService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TransferDeclareCostAllocationMainDTO.UpdateDTO dto) {
        transferDeclareCostAllocationMainService.update(dto);
        return success();
    }



}
