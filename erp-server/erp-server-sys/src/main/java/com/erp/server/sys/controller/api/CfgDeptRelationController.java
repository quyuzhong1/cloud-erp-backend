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
import com.erp.server.sys.service.CfgDeptRelationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.CfgDeptRelationDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.sys.entity.CfgDeptRelationEntity;

/**
 * 部门关联表
 *
 * @author lrp
 * @since 2025-12-29
 */
@Slf4j
@RestController
@LogSystemModule("部门关联表")
@RequestMapping("/cfgDeptRelation")
public class CfgDeptRelationController extends BaseController {

    @Resource
    private CfgDeptRelationService cfgDeptRelationService;

    /**
    * 新增
    * @author lrp
    * @date:  2025-12-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "部门关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgDeptRelationDTO.AddDTO dto) {
        return success(cfgDeptRelationService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2025-12-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "部门关联表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:cfgDeptRelation:update",
        serviceClass = CfgDeptRelationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgDeptRelationDTO.UpdateDTO dto) {
        cfgDeptRelationService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:cfgDeptRelation:paging",
            tableAlias = ""
    )
    public ApiResult<List<CfgDeptRelationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgDeptRelationService.tabList(dto));
    }

    /**
    * 列表查询
    * @author lrp
    * @date: 2025-12-29
    * @param dto
    * @return ApiResult<PagingVO<CfgDeptRelationDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:cfgDeptRelation:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgDeptRelationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgDeptRelationDTO.PagingParamDTO> dto) {
        return success(cfgDeptRelationService.paging(dto));
    }


    /**
    * 详情
    * @author lrp
    * @date:  2025-12-29
    * @param id
    * @return ApiResult<CfgDeptRelationDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:cfgDeptRelation:view",
            serviceClass = CfgDeptRelationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgDeptRelationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgDeptRelationService.view(id));
    }

    /**
    * 导出Excel数据
    * @author lrp
    * @date:  2025-12-29
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:cfgDeptRelation:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "部门关联表导出Excel数据")
    public void exportList(@RequestBody @Validated CfgDeptRelationDTO.ExportDTO dto, HttpServletResponse response) {
        cfgDeptRelationService.exportList(dto, response);
    }


}
