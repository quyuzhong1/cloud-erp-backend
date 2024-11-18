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
import com.erp.server.tms.service.LogisticsServicePlatformService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsServicePlatformDTO;

import java.util.List;

/**
 * 物流平台服务表
 *
 * @author Lambda
 * @since 2024-03-04
 */
@Slf4j
@RestController
@LogSystemModule("物流平台服务表")
@RequestMapping("/logisticsServicePlatform")
public class LogisticsServicePlatformController extends BaseController {

    @Resource
    private LogisticsServicePlatformService logisticsServicePlatformService;

    /**
    * 新增
    * @author Lambda
    * @date:  2024-03-04
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流平台服务表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsServicePlatformDTO.AddDTO dto) {
        return success(logisticsServicePlatformService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2024-03-04
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流平台服务表修改")
    public ApiResult<Object> update(@RequestBody @Validated LogisticsServicePlatformDTO.UpdateDTO dto) {
        logisticsServicePlatformService.update(dto);
        return success();
    }

    /**
     *  logisticsPlatform=AliExpress
     * 下拉列表
     * @description
     * @param logisticsPlatform
     * @return
     * @date 2024-03-05 9:31
     * @author Lambda
     */
    @GetMapping("/listServiceName")
    public ApiResult<List<LogisticsServicePlatformDTO.ServiceNameDTO>>  list(@RequestParam(value = "logisticsPlatform",required = true)String logisticsPlatform){
        return success(logisticsServicePlatformService.listServiceNameByLogisticsPlatform(logisticsPlatform));
    }



}
