package com.erp.server.sys.controller.api;


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
import com.erp.server.sys.service.KingdeeOperatorTypeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeeOperatorTypeDTO;

/**
 * 
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/kingdeeOperatorType")
public class KingdeeOperatorTypeController extends BaseController {

    @Resource
    private KingdeeOperatorTypeService kingdeeOperatorTypeService;

    /**
    * 新增
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KingdeeOperatorTypeDTO.AddDTO dto) {
        return success(kingdeeOperatorTypeService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:kingdeeOperatorType:update",
        serviceClass = KingdeeOperatorTypeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KingdeeOperatorTypeDTO.UpdateDTO dto) {
        kingdeeOperatorTypeService.update(dto);
        return success();
    }



}
