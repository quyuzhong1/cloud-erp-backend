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
import com.erp.server.dmp.service.DmpCfgOutputDataService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgOutputDataDTO;

/**
 * 输出数据获取配置
 *
 * @author shukai
 * @since 2025-02-10
 */
@Slf4j
@RestController
@LogSystemModule("输出数据获取配置")
@RequestMapping("/dmpCfgOutputData")
public class DmpCfgOutputDataController extends BaseController {

    @Resource
    private DmpCfgOutputDataService dmpCfgOutputDataService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-02-10
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "输出数据获取配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgOutputDataDTO.AddDTO dto) {
        return success(dmpCfgOutputDataService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-02-10
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "输出数据获取配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgOutputData:update",
        serviceClass = DmpCfgOutputDataService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgOutputDataDTO.UpdateDTO dto) {
        dmpCfgOutputDataService.update(dto);
        return success();
    }



}
