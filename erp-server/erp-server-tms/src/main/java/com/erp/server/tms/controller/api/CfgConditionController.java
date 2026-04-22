package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.CfgConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgConditionDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.CfgConditionEntity;

/**
 * 条件配置表
 *
 * @author jack
 * @since 2026-04-22
 */
@Slf4j
@RestController
@LogSystemModule("条件配置表")
@RequestMapping("/cfgCondition")
public class CfgConditionController extends BaseController {

    @Resource
    private CfgConditionService cfgConditionService;

    /**
    * 新增
    * @author jack
    * @date:  2026-04-22
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "条件配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgConditionDTO.AddDTO dto) {
        return success(cfgConditionService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2026-04-22
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "条件配置表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:cfgCondition:update",
        serviceClass = CfgConditionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgConditionDTO.UpdateDTO dto) {
        cfgConditionService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgCondition:paging",
            tableAlias = ""
    )
    public ApiResult<List<CfgConditionDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgConditionService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2026-04-22
    * @param dto
    * @return ApiResult<PagingVO<CfgConditionDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgCondition:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgConditionDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgConditionDTO.PagingParamDTO> dto) {
        return success(cfgConditionService.paging(dto));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-04-22
    * @param id
    * @return ApiResult<CfgConditionDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgCondition:view",
            serviceClass = CfgConditionService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgConditionDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgConditionService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2026-04-22
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgCondition:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "条件配置表导出Excel数据")
    public void exportList(@RequestBody @Validated CfgConditionDTO.ExportDTO dto, HttpServletResponse response) {
        cfgConditionService.exportList(dto, response);
    }


}
