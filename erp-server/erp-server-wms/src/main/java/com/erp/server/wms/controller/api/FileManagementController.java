package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.FileManagementDTO;
import com.erp.server.wms.service.FileManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 文件管理
 *
 * @author zdy
 * @since 2026-03-20
 */
@Slf4j
@RestController
@LogSystemModule("文件管理")
@RequestMapping("/fileManagement")
public class FileManagementController extends BaseController {

    @Resource
    private FileManagementService fileManagementService;

    /**
    * 新增
    * @author zdy
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "文件管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FileManagementDTO.AddDTO dto) {
        return success(fileManagementService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "文件管理修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:fileManagement:update",
        serviceClass = FileManagementService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated FileManagementDTO.UpdateDTO dto) {
        fileManagementService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fileManagement:paging",
            tableAlias = ""
    )
    public ApiResult<List<FileManagementDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(fileManagementService.tabList(dto));
    }

    /**
    * 列表查询
    * @author zdy
    * @date: 2026-03-20
    * @param dto
    * @return ApiResult<PagingVO<FileManagementDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fileManagement:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<FileManagementDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FileManagementDTO.PagingParamDTO> dto) {
        return success(fileManagementService.paging(dto));
    }


    /**
    * 详情
    * @author zdy
    * @date:  2026-03-20
    * @param id
    * @return ApiResult<FileManagementDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fileManagement:view",
            serviceClass = FileManagementService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<FileManagementDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(fileManagementService.view(id));
    }

    /**
    * 导出Excel数据
    * @author zdy
    * @date:  2026-03-20
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fileManagement:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "文件管理导出Excel数据")
    public void exportList(@RequestBody @Validated FileManagementDTO.ExportDTO dto, HttpServletResponse response) {
        fileManagementService.exportList(dto, response);
    }


}
