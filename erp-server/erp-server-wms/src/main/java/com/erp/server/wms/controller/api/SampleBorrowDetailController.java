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
import com.erp.server.wms.service.SampleBorrowDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleBorrowDetailDTO;

/**
 * 借用变更单明细表
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@RestController
@LogSystemModule("借用变更单明细表")
@RequestMapping("/sampleBorrowDetail")
public class SampleBorrowDetailController extends BaseController {

    @Resource
    private SampleBorrowDetailService sampleBorrowDetailService;

    /**
    * 新增
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "借用变更单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleBorrowDetailDTO.AddDTO dto) {
        return success(sampleBorrowDetailService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "借用变更单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleBorrowDetail:update",
        serviceClass = SampleBorrowDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleBorrowDetailDTO.UpdateDTO dto) {
        sampleBorrowDetailService.update(dto);
        return success();
    }



}
