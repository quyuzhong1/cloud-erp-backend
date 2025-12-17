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
import com.erp.server.wms.service.SampleReturnDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleReturnDetailDTO;

/**
 * 样品归还单明细表
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@RestController
@LogSystemModule("样品归还单明细表")
@RequestMapping("/sampleReturnDetail")
public class SampleReturnDetailController extends BaseController {

    @Resource
    private SampleReturnDetailService sampleReturnDetailService;

    /**
    * 新增
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品归还单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleReturnDetailDTO.AddDTO dto) {
        return success(sampleReturnDetailService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品归还单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleReturnDetail:update",
        serviceClass = SampleReturnDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleReturnDetailDTO.UpdateDTO dto) {
        sampleReturnDetailService.update(dto);
        return success();
    }



}
