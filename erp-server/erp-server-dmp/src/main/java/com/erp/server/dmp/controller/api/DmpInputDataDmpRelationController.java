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
import com.erp.server.dmp.service.DmpInputDataDmpRelationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpInputDataDmpRelationDTO;

/**
 * data表与dmp关联表
 *
 * @author shukai
 * @since 2024-06-17
 */
@Slf4j
@RestController
@LogSystemModule("data表与dmp关联表")
@RequestMapping("/dmpInputDataDmpRelation")
public class DmpInputDataDmpRelationController extends BaseController {

    @Resource
    private DmpInputDataDmpRelationService dmpInputDataDmpRelationService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-17
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "data表与dmp关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpInputDataDmpRelationDTO.AddDTO dto) {
        return success(dmpInputDataDmpRelationService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-17
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "data表与dmp关联表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpInputDataDmpRelation:update",
        serviceClass = DmpInputDataDmpRelationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpInputDataDmpRelationDTO.UpdateDTO dto) {
        dmpInputDataDmpRelationService.update(dto);
        return success();
    }



}
