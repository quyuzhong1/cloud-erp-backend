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
import com.erp.server.sys.service.DictNoticeRoleOptionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.DictNoticeRoleOptionDTO;

import java.util.List;

/**
 * 
 * 三方通知--通知角色字典
 * @author jack
 * @since 2025-06-04
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/dictNoticeRoleOption")
public class DictNoticeRoleOptionController extends BaseController {

    @Resource
    private DictNoticeRoleOptionService dictNoticeRoleOptionService;

    /**
    * 根据单据类型查询下拉
    * @author jack
    * @date:  2025-06-04
    * @param businessType
    * @return ApiResult<List<DictNoticeRoleOptionDTO.DropDownDTO>>
    */
    @GetMapping("/dropDownList")
    public ApiResult<List<DictNoticeRoleOptionDTO.DropDownDTO>> dropDownList(@RequestParam("businessType")String businessType) {
        return success(dictNoticeRoleOptionService.dropDownList(businessType));
    }


}
