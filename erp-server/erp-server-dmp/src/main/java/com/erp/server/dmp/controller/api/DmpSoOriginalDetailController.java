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
import com.erp.server.dmp.service.DmpSoOriginalDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpSoOriginalDetailDTO;

/**
 * 中台原始销售订单明细表
 *
 * @author shukai
 * @since 2024-11-25
 */
@Slf4j
@RestController
@LogSystemModule("中台原始销售订单明细表")
@RequestMapping("/dmpSoOriginalDetail")
public class DmpSoOriginalDetailController extends BaseController {

    @Resource
    private DmpSoOriginalDetailService dmpSoOriginalDetailService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-11-25
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中台原始销售订单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpSoOriginalDetailDTO.AddDTO dto) {
        return success(dmpSoOriginalDetailService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-11-25
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中台原始销售订单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpSoOriginalDetail:update",
        serviceClass = DmpSoOriginalDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpSoOriginalDetailDTO.UpdateDTO dto) {
        dmpSoOriginalDetailService.update(dto);
        return success();
    }



}
