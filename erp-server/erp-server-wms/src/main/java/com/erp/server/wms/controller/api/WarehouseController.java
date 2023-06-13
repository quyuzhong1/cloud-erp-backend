package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 仓库管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/warehouse")
public class WarehouseController extends BaseController {

    @Resource
    private WarehouseService warehouseService;


    /**
     * 仓库分页列表
     *
     * @param
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:paging",
            tableAlias = "warehouse"
    )
    public ApiResult<PagingVO<WarehouseDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<WarehouseDTO.PagingParamDTO> dto) {
        PagingVO<WarehouseDTO.PagingViewDTO> pagingVO = warehouseService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加仓库
     *
     * @param
     * @return
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:add",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated WarehouseDTO.AddDTO dto) {
        String id = warehouseService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:addAndSubmit",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated WarehouseDTO.AddDTO dto) {
        Boolean result = warehouseService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 仓库提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:submit",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = warehouseService.submit(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 启用仓库
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = warehouseService.updateStatus(dto);
        return result == true ? success() : failure();
    }


    /**
     * 修改仓库
     *
     * @param
     * @return
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:update",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WarehouseDTO.UpdateDTO dto) {
        String id  = warehouseService.updateWarehouse(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }




    /**
     * 修改并提交
     *
     * @param
     * @return
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:updateAndSubmit",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated WarehouseDTO.UpdateDTO dto) {
        Boolean result = warehouseService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 仓库详情
     *
     * @param
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:view",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult<WarehouseDTO.UpdateDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        WarehouseDTO.UpdateDTO view = warehouseService.view(dto.getId());
        return success(view);
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
            menuCode = "wms:warehouse:approve",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = warehouseService.approve(dto);
        return result == true ? success() : failure();
    }

    /**
     * 反审核
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-22 11:56
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:disApprove",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = warehouseService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }


    /**
     * 删除仓库
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:delete",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = warehouseService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 导出
     * 仓库数据
     */
    @PostMapping("/exportWarehouse")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:paging",
            tableAlias = "warehouse"
    )
    public ApiResult exportWarehouse(@RequestBody @Valid WarehouseDTO.ExportDTO dto, HttpServletResponse response) {
        warehouseService.exportWarehouse(dto, response);
        return success();
    }

    /**
     * 导入仓库
     */
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = warehouseService.importFile(excelFile, response);
        return result == true ? success() : failure();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        warehouseService.downloadTemplate(response);
        return success();
    }


    /**
     * 仓库列表
     */
    @GetMapping("/list")
    public ApiResult<List<WarehouseDTO.ListDTO>> list() {
        List<WarehouseDTO.ListDTO> list = warehouseService.listApproveWarehouse();
        return success(list);
    }
}
