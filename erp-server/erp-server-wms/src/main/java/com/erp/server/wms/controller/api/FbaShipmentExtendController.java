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
import com.erp.server.wms.service.FbaShipmentExtendService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.FbaShipmentExtendDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.FbaShipmentExtendEntity;

/**
 * FBA拣货扩展表
 *
 * @author zdy
 * @since 2025-12-24
 */
@Slf4j
@RestController
@LogSystemModule("FBA拣货扩展表")
@RequestMapping("/fbaShipmentExtend")
public class FbaShipmentExtendController extends BaseController {

    @Resource
    private FbaShipmentExtendService fbaShipmentExtendService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-12-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "FBA拣货扩展表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FbaShipmentExtendDTO.AddDTO dto) {
        return success(fbaShipmentExtendService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-12-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "FBA拣货扩展表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:fbaShipmentExtend:update",
        serviceClass = FbaShipmentExtendService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated FbaShipmentExtendDTO.UpdateDTO dto) {
        fbaShipmentExtendService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaShipmentExtend:paging",
            tableAlias = ""
    )
    public ApiResult<List<FbaShipmentExtendDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(fbaShipmentExtendService.tabList(dto));
    }

    /**
    * 列表查询
    * @author zdy
    * @date: 2025-12-24
    * @param dto
    * @return ApiResult<PagingVO<FbaShipmentExtendDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaShipmentExtend:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<FbaShipmentExtendDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbaShipmentExtendDTO.PagingParamDTO> dto) {
        return success(fbaShipmentExtendService.paging(dto));
    }


    /**
    * 详情
    * @author zdy
    * @date:  2025-12-24
    * @param id
    * @return ApiResult<FbaShipmentExtendDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaShipmentExtend:view",
            serviceClass = FbaShipmentExtendService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<FbaShipmentExtendDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(fbaShipmentExtendService.view(id));
    }

    /**
    * 导出Excel数据
    * @author zdy
    * @date:  2025-12-24
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaShipmentExtend:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "FBA拣货扩展表导出Excel数据")
    public void exportList(@RequestBody @Validated FbaShipmentExtendDTO.ExportDTO dto, HttpServletResponse response) {
        fbaShipmentExtendService.exportList(dto, response);
    }


}
