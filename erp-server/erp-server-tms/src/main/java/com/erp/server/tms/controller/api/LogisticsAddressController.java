package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsAddressService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsAddressDTO;

/**
 * 物流地址
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("物流地址")
@RequestMapping("/logisticsAddress")
public class LogisticsAddressController extends BaseController {

    @Autowired
    private LogisticsAddressService logisticsAddressService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流地址表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsAddressDTO.AddDTO dto) {
        return success(logisticsAddressService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsAddress:update",
            serviceClass = LogisticsAddressService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsAddressDTO.UpdateDTO dto) {
        logisticsAddressService.update(dto);
        return success();
    }

    /**
     * 修改
     *
     * @param id
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-02
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsAddress:view",
            serviceClass = LogisticsAddressService.class,
            keyIdName = "id")
    public ApiResult<LogisticsAddressDTO.ViewDTO> view(@RequestParam("id") String id) {
        LogisticsAddressDTO.ViewDTO view = logisticsAddressService.view(id);
        return success(view);
    }


}
