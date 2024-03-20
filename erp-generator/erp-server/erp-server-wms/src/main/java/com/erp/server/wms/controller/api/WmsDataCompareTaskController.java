package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.WmsDataCompareTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;

/**
 * 数据对比任务
 *
 * @author shukai
 * @since 2024-03-20
 */
@Slf4j
@RestController
@LogSystemModule("数据对比任务")
@RequestMapping("/wmsDataCompareTask")
public class WmsDataCompareTaskController extends BaseController {

    @Resource
    private WmsDataCompareTaskService wmsDataCompareTaskService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-03-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "数据对比任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated WmsDataCompareTaskDTO.AddDTO dto) {
        return success(wmsDataCompareTaskService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-03-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "数据对比任务修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:wmsDataCompareTask:update",
        serviceClass = WmsDataCompareTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated WmsDataCompareTaskDTO.UpdateDTO dto) {
        wmsDataCompareTaskService.update(dto);
        return success();
    }



}
