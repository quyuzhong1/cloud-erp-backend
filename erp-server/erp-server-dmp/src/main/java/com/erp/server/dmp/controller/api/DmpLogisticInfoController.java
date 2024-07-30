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
import com.erp.server.dmp.service.DmpLogisticInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpLogisticInfoDTO;

/**
 * 中台销售订单出库库位详情
 *
 * @author shukai
 * @since 2024-07-30
 */
@Slf4j
@RestController
@LogSystemModule("中台销售订单出库库位详情")
@RequestMapping("/dmpLogisticInfo")
public class DmpLogisticInfoController extends BaseController {

    @Resource
    private DmpLogisticInfoService dmpLogisticInfoService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-07-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中台销售订单出库库位详情新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpLogisticInfoDTO.AddDTO dto) {
        return success(dmpLogisticInfoService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-07-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中台销售订单出库库位详情修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpLogisticInfo:update",
        serviceClass = DmpLogisticInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpLogisticInfoDTO.UpdateDTO dto) {
        dmpLogisticInfoService.update(dto);
        return success();
    }



}
