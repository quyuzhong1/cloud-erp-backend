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
import com.erp.server.tms.service.CfgLogisticsCostImportDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;

/**
 * 费用项配置字段配置
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@RestController
@LogSystemModule("费用项配置字段配置")
@RequestMapping("/cfgLogisticsCostImportDetail")
public class CfgLogisticsCostImportDetailController extends BaseController {

    @Resource
    private CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService;

    /**
    * 新增
    * @author jack
    * @date:  2026-01-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "费用项配置字段配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgLogisticsCostImportDetailDTO.AddDTO dto) {
        return success(cfgLogisticsCostImportDetailService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2026-01-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "费用项配置字段配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:cfgLogisticsCostImportDetail:update",
        serviceClass = CfgLogisticsCostImportDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgLogisticsCostImportDetailDTO.UpdateDTO dto) {
        cfgLogisticsCostImportDetailService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImportDetail:paging",
            tableAlias = ""
    )
    public ApiResult<List<CfgLogisticsCostImportDetailDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgLogisticsCostImportDetailService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return ApiResult<PagingVO<CfgLogisticsCostImportDetailDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImportDetail:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgLogisticsCostImportDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgLogisticsCostImportDetailDTO.PagingParamDTO> dto) {
        return success(cfgLogisticsCostImportDetailService.paging(dto));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-01-20
    * @param id
    * @return ApiResult<CfgLogisticsCostImportDetailDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImportDetail:view",
            serviceClass = CfgLogisticsCostImportDetailService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgLogisticsCostImportDetailDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgLogisticsCostImportDetailService.view(id));
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
            menuCode = "tms:cfgLogisticsCostImportDetail:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "费用项配置字段配置导出Excel数据")
    public void exportList(@RequestBody @Validated CfgLogisticsCostImportDetailDTO.ExportDTO dto, HttpServletResponse response) {
        cfgLogisticsCostImportDetailService.exportList(dto, response);
    }


}
