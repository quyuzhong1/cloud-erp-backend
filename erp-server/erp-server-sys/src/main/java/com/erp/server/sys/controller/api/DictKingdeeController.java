package com.erp.server.sys.controller.api;


import com.erp.model.workflow.dto.DictBasicDTO;
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
import com.erp.server.sys.service.DictKingdeeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.DictKingdeeDTO;

import java.util.List;

/**
 * 金蝶字典表
 *
 * @author lrp
 * @since 2024-06-07
 */
@Slf4j
@RestController
@LogSystemModule("金蝶字典表")
@RequestMapping("/dictKingdee")
public class DictKingdeeController extends BaseController {

    @Resource
    private DictKingdeeService dictKingdeeService;

    /**
     *
     * @return
     */
    @PostMapping("/listByParam")
    public ApiResult<List<DictKingdeeDTO.ListDTO>> listByParam(@RequestBody DictKingdeeDTO.ParamDTO param)  {
        return success(dictKingdeeService.listByParam(param));
    }



    /**
     * 下拉列表
     * @return
     */
    @GetMapping("/drop/down")
    public ApiResult<List<DictKingdeeDTO.ListDTO>> dropDown(@RequestParam(value = "typeName", required = false) String typeName) {
        return success(dictKingdeeService.dropDown(typeName));
    }
}
