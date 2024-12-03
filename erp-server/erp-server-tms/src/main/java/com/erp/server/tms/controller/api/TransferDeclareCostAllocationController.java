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
import com.erp.server.tms.service.TransferDeclareCostAllocationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;

/**
 * 中转费用分摊
 *
 * @author shukai
 * @since 2024-12-03
 */
@Slf4j
@RestController
@LogSystemModule("中转费用分摊")
@RequestMapping("/transferDeclareCostAllocation")
public class TransferDeclareCostAllocationController extends BaseController {

    @Resource
    private TransferDeclareCostAllocationService transferDeclareCostAllocationService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-12-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转费用分摊新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TransferDeclareCostAllocationDTO.AddDTO dto) {
        return success(transferDeclareCostAllocationService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-12-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中转费用分摊修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:transferDeclareCostAllocation:update",
        serviceClass = TransferDeclareCostAllocationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TransferDeclareCostAllocationDTO.UpdateDTO dto) {
        transferDeclareCostAllocationService.update(dto);
        return success();
    }



}
