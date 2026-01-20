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
import com.erp.server.tms.service.CfgLogisticsCostImportFieldService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgLogisticsCostImportFieldDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;

/**
 * 费用项配置字段基础表
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@RestController
@LogSystemModule("费用项配置字段基础表")
@RequestMapping("/cfgLogisticsCostImportField")
public class CfgLogisticsCostImportFieldController extends BaseController {

    @Resource
    private CfgLogisticsCostImportFieldService cfgLogisticsCostImportFieldService;

    /**
    * 新增
    * @author jack
    * @date:  2026-01-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "费用项配置字段基础表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgLogisticsCostImportFieldDTO.AddDTO dto) {
        return success(cfgLogisticsCostImportFieldService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2026-01-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "费用项配置字段基础表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:cfgLogisticsCostImportField:update",
        serviceClass = CfgLogisticsCostImportFieldService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgLogisticsCostImportFieldDTO.UpdateDTO dto) {
        cfgLogisticsCostImportFieldService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImportField:paging",
            tableAlias = ""
    )
    public ApiResult<List<CfgLogisticsCostImportFieldDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgLogisticsCostImportFieldService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return ApiResult<PagingVO<CfgLogisticsCostImportFieldDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImportField:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgLogisticsCostImportFieldDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgLogisticsCostImportFieldDTO.PagingParamDTO> dto) {
        return success(cfgLogisticsCostImportFieldService.paging(dto));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-01-20
    * @param id
    * @return ApiResult<CfgLogisticsCostImportFieldDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImportField:view",
            serviceClass = CfgLogisticsCostImportFieldService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgLogisticsCostImportFieldDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgLogisticsCostImportFieldService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2026-01-20
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImportField:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "费用项配置字段基础表导出Excel数据")
    public void exportList(@RequestBody @Validated CfgLogisticsCostImportFieldDTO.ExportDTO dto, HttpServletResponse response) {
        cfgLogisticsCostImportFieldService.exportList(dto, response);
    }


}
