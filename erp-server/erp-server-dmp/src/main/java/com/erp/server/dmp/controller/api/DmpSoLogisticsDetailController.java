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
import com.erp.server.dmp.service.DmpSoLogisticsDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpSoLogisticsDetailDTO;

/**
 * 中台物流单明细表
 *
 * @author shukai
 * @since 2025-04-07
 */
@Slf4j
@RestController
@LogSystemModule("中台物流单明细表")
@RequestMapping("/dmpSoLogisticsDetail")
public class DmpSoLogisticsDetailController extends BaseController {

    @Resource
    private DmpSoLogisticsDetailService dmpSoLogisticsDetailService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-04-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中台物流单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpSoLogisticsDetailDTO.AddDTO dto) {
        return success(dmpSoLogisticsDetailService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-04-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中台物流单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpSoLogisticsDetail:update",
        serviceClass = DmpSoLogisticsDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpSoLogisticsDetailDTO.UpdateDTO dto) {
        dmpSoLogisticsDetailService.update(dto);
        return success();
    }



}
