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
import com.erp.server.tms.service.CfgDeclareRuleConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgDeclareRuleConditionDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.CfgDeclareRuleConditionEntity;

/**
 * 报关规则条件表
 *
 * @author jack
 * @since 2026-04-20
 */
@Slf4j
@RestController
@LogSystemModule("报关规则条件表")
@RequestMapping("/cfgDeclareRuleCondition")
public class CfgDeclareRuleConditionController extends BaseController {

    @Resource
    private CfgDeclareRuleConditionService cfgDeclareRuleConditionService;

    /**
    * 新增
    * @author jack
    * @date:  2026-04-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "报关规则条件表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgDeclareRuleConditionDTO.AddDTO dto) {
        return success(cfgDeclareRuleConditionService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2026-04-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "报关规则条件表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:cfgDeclareRuleCondition:update",
        serviceClass = CfgDeclareRuleConditionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgDeclareRuleConditionDTO.UpdateDTO dto) {
        cfgDeclareRuleConditionService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgDeclareRuleCondition:paging",
            tableAlias = ""
    )
    public ApiResult<List<CfgDeclareRuleConditionDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgDeclareRuleConditionService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @return ApiResult<PagingVO<CfgDeclareRuleConditionDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgDeclareRuleCondition:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgDeclareRuleConditionDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgDeclareRuleConditionDTO.PagingParamDTO> dto) {
        return success(cfgDeclareRuleConditionService.paging(dto));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-04-20
    * @param id
    * @return ApiResult<CfgDeclareRuleConditionDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgDeclareRuleCondition:view",
            serviceClass = CfgDeclareRuleConditionService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgDeclareRuleConditionDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgDeclareRuleConditionService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2026-04-20
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgDeclareRuleCondition:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "报关规则条件表导出Excel数据")
    public void exportList(@RequestBody @Validated CfgDeclareRuleConditionDTO.ExportDTO dto, HttpServletResponse response) {
        cfgDeclareRuleConditionService.exportList(dto, response);
    }


}
