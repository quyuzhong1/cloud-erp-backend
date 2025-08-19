package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsWarehouseDTO;
import com.erp.server.tms.service.LogisticsWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 海外仓物流商 仓库表
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("海外仓物流商仓库表")
@RequestMapping("/logisticsWarehouse")
public class LogisticsWarehouseController extends BaseController {

    @Resource
    private LogisticsWarehouseService logisticsWarehouseService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "海外仓物流商 仓库表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsWarehouseDTO.AddDTO dto) {
        return success(logisticsWarehouseService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsWarehouse:update",
        serviceClass = LogisticsWarehouseService.class,
        keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改海外仓物流商 仓库单数据")
    public ApiResult<Object>update(@RequestBody @Validated LogisticsWarehouseDTO.UpdateDTO dto) {
        logisticsWarehouseService.update(dto);
        return success();
    }



}
