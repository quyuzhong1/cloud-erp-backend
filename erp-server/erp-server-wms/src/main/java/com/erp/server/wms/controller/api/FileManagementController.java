package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
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
     *
     * @param dto
     * @return ApiResult<String>
     * @author zdy
     * @date: 2026-03-20
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "文件管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FileManagementDTO.AddDTO dto) {
        return success(fileManagementService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author zdy
     * @date: 2026-03-20
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "文件管理修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fileManagement:update",
            serviceClass = FileManagementService.class,
            keyIdName = "id")
    public ApiResult<Boolean> update(@RequestBody @Validated FileManagementDTO.UpdateDTO dto) {
        return success(fileManagementService.update(dto));
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult<PagingVO < FileManagementDTO.ListDTO>>
     * @author zdy
     * @date: 2026-03-20
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fileManagement:paging",
            tableAlias = "fm"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<FileManagementDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FileManagementDTO.PagingParamDTO> dto) {
        return success(fileManagementService.paging(dto));
    }


    /**
     * 详情
     *
     * @param id
     * @return ApiResult<FileManagementDTO.ViewDTO>>
     * @author zdy
     * @date: 2026-03-20
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
     * 版本记录
     *
     * @param id
     * @return
     */
    @GetMapping("/history")
    public ApiResult<List<FileManagementDTO.VersionDTO>> history(@RequestParam("id") String id) {
        return success(fileManagementService.history(id));
    }
    /**
     * 批量生成质检标准
     * ids 取值 skuRefId
     */
    @PostMapping("/genQcStandard")
    public ApiResult<List<BatchResultDTO>> genQcStandard(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        return success(fileManagementService.genQcStandard(idsDTO.getIds()));
    }
    /**
     * 生成单个质检标准
     *
     * @param id 取值 skuRefId
     * @return
     */
    @GetMapping("/genSingleQcStandard")
    public ApiResult<Boolean> genSingleQcStandard(@RequestParam("id") String id) {
        return success(fileManagementService.genSingleQcStandard(id));
    }
}
