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
import com.erp.server.wms.service.WmsDataCompareTempService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.WmsDataCompareTempDTO;

/**
 * 数据对比对比加工临时表
 *
 * @author shukai
 * @since 2024-03-20
 */
@Slf4j
@RestController
@LogSystemModule("数据对比对比加工临时表")
@RequestMapping("/wmsDataCompareTemp")
public class WmsDataCompareTempController extends BaseController {

    @Resource
    private WmsDataCompareTempService wmsDataCompareTempService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-03-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "数据对比对比加工临时表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated WmsDataCompareTempDTO.AddDTO dto) {
        return success(wmsDataCompareTempService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-03-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "数据对比对比加工临时表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:wmsDataCompareTemp:update",
        serviceClass = WmsDataCompareTempService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated WmsDataCompareTempDTO.UpdateDTO dto) {
        wmsDataCompareTempService.update(dto);
        return success();
    }



}
