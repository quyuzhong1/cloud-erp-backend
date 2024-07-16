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
import com.erp.server.dmp.service.DmpInputFileMongoRelationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpInputFileMongoRelationDTO;

/**
 * file与mongo关联表
 *
 * @author shukai
 * @since 2024-06-19
 */
@Slf4j
@RestController
@LogSystemModule("file与mongo关联表")
@RequestMapping("/dmpInputFileMongoRelation")
public class DmpInputFileMongoRelationController extends BaseController {

    @Resource
    private DmpInputFileMongoRelationService dmpInputFileMongoRelationService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "file与mongo关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpInputFileMongoRelationDTO.AddDTO dto) {
        return success(dmpInputFileMongoRelationService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "file与mongo关联表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpInputFileMongoRelation:update",
        serviceClass = DmpInputFileMongoRelationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpInputFileMongoRelationDTO.UpdateDTO dto) {
        dmpInputFileMongoRelationService.update(dto);
        return success();
    }



}
