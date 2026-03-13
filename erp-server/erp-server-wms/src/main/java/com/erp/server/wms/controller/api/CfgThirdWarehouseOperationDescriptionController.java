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
import com.erp.server.wms.service.CfgThirdWarehouseOperationDescriptionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.CfgThirdWarehouseOperationDescriptionDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.CfgThirdWarehouseOperationDescriptionEntity;

/**
 * 
 *
 * @author wtr
 * @since 2026-03-13
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/cfgThirdWarehouseOperationDescription")
public class CfgThirdWarehouseOperationDescriptionController extends BaseController {

    @Resource
    private CfgThirdWarehouseOperationDescriptionService cfgThirdWarehouseOperationDescriptionService;

    /**
    * 新增
    * @author wtr
    * @date:  2026-03-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgThirdWarehouseOperationDescriptionDTO.AddDTO dto) {
        return success(cfgThirdWarehouseOperationDescriptionService.add(dto));
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
        menuCode = "wms:cfgThirdWarehouseOperationDescription:update",
        serviceClass = CfgThirdWarehouseOperationDescriptionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgThirdWarehouseOperationDescriptionDTO.UpdateDTO dto) {
        cfgThirdWarehouseOperationDescriptionService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:cfgThirdWarehouseOperationDescription:paging",
            tableAlias = ""
    )
    public ApiResult<List<CfgThirdWarehouseOperationDescriptionDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgThirdWarehouseOperationDescriptionService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @return ApiResult<PagingVO<CfgThirdWarehouseOperationDescriptionDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:cfgThirdWarehouseOperationDescription:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgThirdWarehouseOperationDescriptionDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgThirdWarehouseOperationDescriptionDTO.PagingParamDTO> dto) {
        return success(cfgThirdWarehouseOperationDescriptionService.paging(dto));
    }


    /**
    * 详情
    * @author wtr
    * @date:  2026-03-13
    * @param id
    * @return ApiResult<CfgThirdWarehouseOperationDescriptionDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:cfgThirdWarehouseOperationDescription:view",
            serviceClass = CfgThirdWarehouseOperationDescriptionService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgThirdWarehouseOperationDescriptionDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgThirdWarehouseOperationDescriptionService.view(id));
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
            menuCode = "wms:cfgThirdWarehouseOperationDescription:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    public void exportList(@RequestBody @Validated CfgThirdWarehouseOperationDescriptionDTO.ExportDTO dto, HttpServletResponse response) {
        cfgThirdWarehouseOperationDescriptionService.exportList(dto, response);
    }


}
