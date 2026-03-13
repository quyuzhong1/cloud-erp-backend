package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.CfgThirdWarehouseOperationDescriptionValueService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.CfgThirdWarehouseOperationDescriptionValueDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.CfgThirdWarehouseOperationDescriptionValueEntity;

/**
 * 
 *
 * @author wtr
 * @since 2026-03-13
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/cfgThirdWarehouseOperationDescriptionValue")
public class CfgThirdWarehouseOperationDescriptionValueController extends BaseController {

    @Resource
    private CfgThirdWarehouseOperationDescriptionValueService cfgThirdWarehouseOperationDescriptionValueService;

    /**
    * 新增
    * @author wtr
    * @date:  2026-03-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgThirdWarehouseOperationDescriptionValueDTO.AddDTO dto) {
        return success(cfgThirdWarehouseOperationDescriptionValueService.add(dto));
    }

    /**
    * 修改
    * @author wtr
    * @date:  2026-03-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:cfgThirdWarehouseOperationDescriptionValue:update",
        serviceClass = CfgThirdWarehouseOperationDescriptionValueService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgThirdWarehouseOperationDescriptionValueDTO.UpdateDTO dto) {
        cfgThirdWarehouseOperationDescriptionValueService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:cfgThirdWarehouseOperationDescriptionValue:paging",
            tableAlias = ""
    )
    public ApiResult<List<CfgThirdWarehouseOperationDescriptionValueDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgThirdWarehouseOperationDescriptionValueService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @return ApiResult<PagingVO<CfgThirdWarehouseOperationDescriptionValueDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:cfgThirdWarehouseOperationDescriptionValue:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgThirdWarehouseOperationDescriptionValueDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgThirdWarehouseOperationDescriptionValueDTO.PagingParamDTO> dto) {
        return success(cfgThirdWarehouseOperationDescriptionValueService.paging(dto));
    }


    /**
    * 详情
    * @author wtr
    * @date:  2026-03-13
    * @param id
    * @return ApiResult<CfgThirdWarehouseOperationDescriptionValueDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:cfgThirdWarehouseOperationDescriptionValue:view",
            serviceClass = CfgThirdWarehouseOperationDescriptionValueService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgThirdWarehouseOperationDescriptionValueDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgThirdWarehouseOperationDescriptionValueService.view(id));
    }

    /**
    * 导出Excel数据
    * @author wtr
    * @date:  2026-03-13
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:cfgThirdWarehouseOperationDescriptionValue:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    public void exportList(@RequestBody @Validated CfgThirdWarehouseOperationDescriptionValueDTO.ExportDTO dto, HttpServletResponse response) {
        cfgThirdWarehouseOperationDescriptionValueService.exportList(dto, response);
    }


}
