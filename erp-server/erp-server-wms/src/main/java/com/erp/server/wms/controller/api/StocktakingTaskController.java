package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.server.wms.service.StocktakingTaskService;
import com.erp.server.wms.service.TransferInService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 *
 * 盘点管理-盘点任务
 *
 *
 * @author Lambda
 * @since 2023-07-31
 */
@RestController
@RequestMapping("/stocktakingTask")
public class StocktakingTaskController extends BaseController {

    @Resource
    private StocktakingTaskService stocktakingTaskService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<StocktakingTaskDTO.TabDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<StocktakingTaskDTO.TabDTO> tabList = stocktakingTaskService.tabList(dto);
        return success(tabList);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:paging",
            tableAlias = "ti"
    )
    public ApiResult<PagingVO<StocktakingTaskDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<StocktakingTaskDTO.PagingParamDTO> dto) {
        PagingVO<StocktakingTaskDTO.PagingViewDTO> pagingVO = stocktakingTaskService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:submit",
            serviceClass = StocktakingTaskService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = stocktakingTaskService.submit(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:view",
            serviceClass = StocktakingTaskService.class,
            keyIdName = "id"
    )
    public ApiResult<StocktakingTaskDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        StocktakingTaskDTO.ViewDTO result = stocktakingTaskService.view(dto.getId());
        return success(result);
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:approve",
            serviceClass = StocktakingTaskService.class,
            keyIdName = "ids"
    )
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = stocktakingTaskService.approve(dto);
        return result ? success() : failure();

    }

    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:cancelProcess",
            serviceClass = StocktakingTaskService.class,
            keyIdName = "ids"
    )
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = stocktakingTaskService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }

    @PostMapping("/assignUser")
    public ApiResult assignStocktakingUser(@RequestBody @Validated StocktakingTaskDTO.AssignUserDTO dto){
        Boolean result = stocktakingTaskService.assignUser(dto);
        return result ? success() : failure();
    }

    /**
     * 导出
     * 数据
     */
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid StocktakingTaskDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = stocktakingTaskService.exportExcel(dto, response);
        return result ? success() : failure();
    }

    /**
     * 导入
     * 数据
     */
    @PostMapping("/import")
    public ApiResult exportWarehouse(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = stocktakingTaskService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        stocktakingTaskService.downloadTemplate(response);
        return success();
    }

}
