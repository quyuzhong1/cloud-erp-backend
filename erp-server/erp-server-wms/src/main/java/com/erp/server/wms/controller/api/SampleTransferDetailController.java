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
import com.erp.server.wms.service.SampleTransferDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleTransferDetailDTO;

/**
 * 样品转移单明细表
 *
 * @author wuhaotian
 * @since 2025-10-28
 */
@Slf4j
@RestController
@LogSystemModule("样品转移单明细表")
@RequestMapping("/sampleTransferDetail")
public class SampleTransferDetailController extends BaseController {

    @Resource
    private SampleTransferDetailService sampleTransferDetailService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品转移单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleTransferDetailDTO.AddDTO dto) {
        return success(sampleTransferDetailService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品转移单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleTransferDetail:update",
        serviceClass = SampleTransferDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleTransferDetailDTO.UpdateDTO dto) {
        sampleTransferDetailService.update(dto);
        return success();
    }



}
