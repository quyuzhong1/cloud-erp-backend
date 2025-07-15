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
import com.erp.server.sys.service.SysDepartmentThirdService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.SysDepartmentThirdDTO;

import java.util.List;

/**
 * 第三方 部门信息
 *
 * @author jack
 * @since 2025-05-15
 */
@Slf4j
@RestController
@LogSystemModule("第三方 部门信息")
@RequestMapping("/sysDepartmentThird")
public class SysDepartmentThirdController extends BaseController {

    @Resource
    private SysDepartmentThirdService sysDepartmentThirdService;

    /**
     * 第三方部门下拉查询
     * @author jack
     * @date:  2025-05-15
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/listThirdDeptDropDown")
    public ApiResult<List<SysDepartmentThirdDTO.ThirdDeptDropDownDTO>> listThirdDeptDropDown(@RequestBody @Validated SysDepartmentThirdDTO.ThirdDeptParamDTO dto) {
      List<SysDepartmentThirdDTO.ThirdDeptDropDownDTO> list = sysDepartmentThirdService.listThirdDeptDropDown(dto);
      return success(list);
    }

    /**
     * 飞书部门同步
     * @author jack
     * @date:  2025-05-15
     * @return ApiResult
     */
    @PostMapping("/syncFsDept")
    public void syncFsDept() {
        sysDepartmentThirdService.syncFsDept();
    }



}
