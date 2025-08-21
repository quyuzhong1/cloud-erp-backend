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
import com.erp.server.wms.service.SampleRecipientDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleRecipientDetailDTO;

/**
 * 样品领用单明细
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@RestController
@LogSystemModule("样品领用单明细")
@RequestMapping("/sampleRecipientDetail")
public class SampleRecipientDetailController extends BaseController {

    @Resource
    private SampleRecipientDetailService sampleRecipientDetailService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品领用单明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleRecipientDetailDTO.AddDTO dto) {
        return success(sampleRecipientDetailService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品领用单明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleRecipientDetail:update",
        serviceClass = SampleRecipientDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleRecipientDetailDTO.UpdateDTO dto) {
        sampleRecipientDetailService.update(dto);
        return success();
    }



}
