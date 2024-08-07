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
import com.erp.server.dmp.service.DmpThirdRegionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpThirdRegionDTO;

/**
 * 第三方区域
 *
 * @author shukai
 * @since 2024-08-07
 */
@Slf4j
@RestController
@LogSystemModule("第三方区域")
@RequestMapping("/dmpThirdRegion")
public class DmpThirdRegionController extends BaseController {

    @Resource
    private DmpThirdRegionService dmpThirdRegionService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-08-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "第三方区域新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpThirdRegionDTO.AddDTO dto) {
        return success(dmpThirdRegionService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-08-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方区域修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpThirdRegion:update",
        serviceClass = DmpThirdRegionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpThirdRegionDTO.UpdateDTO dto) {
        dmpThirdRegionService.update(dto);
        return success();
    }



}
