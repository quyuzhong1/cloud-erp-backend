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
import com.erp.server.sys.service.KingdeeOperatorRefPostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;

/**
 * 金蝶业务员表
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("金蝶业务员表")
@RequestMapping("/kingdeeOperatorRefPost")
public class KingdeeOperatorRefPostController extends BaseController {

    @Resource
    private KingdeeOperatorRefPostService kingdeeOperatorRefPostService;

    /**
    * 新增
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "金蝶业务员表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KingdeeOperatorRefPostDTO.AddDTO dto) {
        return success(kingdeeOperatorRefPostService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "金蝶业务员表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:kingdeeOperatorRefPost:update",
        serviceClass = KingdeeOperatorRefPostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KingdeeOperatorRefPostDTO.UpdateDTO dto) {
        kingdeeOperatorRefPostService.update(dto);
        return success();
    }



}
