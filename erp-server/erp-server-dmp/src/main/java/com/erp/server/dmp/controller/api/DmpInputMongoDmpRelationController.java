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
import com.erp.server.dmp.service.DmpInputMongoDmpRelationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpInputMongoDmpRelationDTO;

/**
 * mongo与dmp关联表
 *
 * @author shukai
 * @since 2024-06-19
 */
@Slf4j
@RestController
@LogSystemModule("mongo与dmp关联表")
@RequestMapping("/dmpInputMongoDmpRelation")
public class DmpInputMongoDmpRelationController extends BaseController {

    @Resource
    private DmpInputMongoDmpRelationService dmpInputMongoDmpRelationService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "mongo与dmp关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpInputMongoDmpRelationDTO.AddDTO dto) {
        return success(dmpInputMongoDmpRelationService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "mongo与dmp关联表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpInputMongoDmpRelation:update",
        serviceClass = DmpInputMongoDmpRelationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpInputMongoDmpRelationDTO.UpdateDTO dto) {
        dmpInputMongoDmpRelationService.update(dto);
        return success();
    }



}
