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
import com.erp.server.sys.service.KingdeeUserRefPostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeeUserRefPostDTO;

/**
 * 金蝶员工任岗表
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("金蝶员工任岗表")
@RequestMapping("/kingdeeUserRefPost")
public class KingdeeUserRefPostController extends BaseController {

    @Resource
    private KingdeeUserRefPostService kingdeeUserRefPostService;

    /**
    * 新增
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "金蝶员工任岗表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KingdeeUserRefPostDTO.AddDTO dto) {
        return success(kingdeeUserRefPostService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "金蝶员工任岗表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:kingdeeUserRefPost:update",
        serviceClass = KingdeeUserRefPostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KingdeeUserRefPostDTO.UpdateDTO dto) {
        kingdeeUserRefPostService.update(dto);
        return success();
    }



}
