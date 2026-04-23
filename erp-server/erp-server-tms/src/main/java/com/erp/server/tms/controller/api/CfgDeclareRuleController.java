package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;
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
import com.erp.server.tms.service.CfgDeclareRuleService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 报关规则主表
 *
 * @author jack
 * @since 2026-04-20
 */
@Slf4j
@RestController
@LogSystemModule("报关规则主表")
@RequestMapping("/cfgDeclareRule")
public class CfgDeclareRuleController extends BaseController {

    @Resource
    private CfgDeclareRuleService cfgDeclareRuleService;

    /**
    * 新增
    * @author jack
    * @date:  2026-04-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "报关规则主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgDeclareRuleDTO.AddDTO dto) {
        return success(cfgDeclareRuleService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2026-04-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "报关规则主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:cfgDeclareRule:update",
        serviceClass = CfgDeclareRuleService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgDeclareRuleDTO.UpdateDTO dto) {
        cfgDeclareRuleService.update(dto);
        return success();
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @return ApiResult<PagingVO<CfgDeclareRuleDTO.ListDTO>>
    */
    @PostMapping("/paging")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "tms:cfgDeclareRule:paging",
//            tableAlias = ""
//    )
    public ApiResult<PagingVO<CfgDeclareRuleDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgDeclareRuleDTO.ListParamDTO> dto) {
        return success(cfgDeclareRuleService.paging(dto));
    }

    /**
     * 获取报关规则主体下拉列表
     * 用于页面下拉选择框，支持按发件人/收件人类型筛选，可选按名称搜索会计公司
     *
     * @param type 类型：sender-发件人，receiver-收件人
     * @param name 会计公司名称（可选，用于模糊搜索）
     * @return 报关规则主体下拉列表（树形结构）
     * @author jack
     * @date 2026-04-20
     */
    @GetMapping("/dropDownList")
    public ApiResult<List<BaseDropDownDTO.Tree>> dropDownList(@RequestParam("type") String type,
                                                              @RequestParam(value = "name", required = false) String name) {
        return success(cfgDeclareRuleService.dropDownList(type, name));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-04-20
    * @param id
    * @return ApiResult<CfgDeclareRuleDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgDeclareRule:view",
            serviceClass = CfgDeclareRuleService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgDeclareRuleDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgDeclareRuleService.view(id));
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
            menuCode = "tms:cfgDeclareRule:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "报关规则主表导出Excel数据")
    public void exportList(@RequestBody @Validated CfgDeclareRuleDTO.ExportDTO dto, HttpServletResponse response) {
        cfgDeclareRuleService.exportList(dto, response);
    }
}
