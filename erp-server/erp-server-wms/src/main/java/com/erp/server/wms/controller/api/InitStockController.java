package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import com.erp.server.wms.service.InitStockService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 期初库存管理
 * @author zhangchunlin
 * @since 2023-05-11
 */
@AllArgsConstructor
@RestController
@RequestMapping(value = "/initStock")
public class InitStockController extends BaseController {

    private final InitStockService initStockService;

    /**
     * 分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:initStock:paging",
            tableAlias = "ism"
    )
    public ApiResult<PagingVO<InitStockDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<InitStockDTO.SearchParamDTO> dto) {
        return success(initStockService.paging(dto));
    }

    /**
     * 新增
     * @param dto
     * @return
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:add",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<Void> add(@RequestBody @Validated InitStockDTO.AddDTO dto) {
        initStockService.add(dto);
        return  success();
    }

    /**
     * 修改
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:update",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated InitStockDTO.UpdateDTO dto) {
        initStockService.update(dto);
        return  success();
    }

    /**
     * 新增并提交
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:add",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated InitStockDTO.AddDTO dto) {
        initStockService.addAndSubmit(dto);
        return  success();
    }

    /**
     * 修改并提交
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:update",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated InitStockDTO.UpdateDTO dto) {
        initStockService.updateAndSubmit(dto);
        return  success();
    }


    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:view",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<InitStockDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(initStockService.view(id));
    }

    /**
     * 提交审核
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:submit",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        initStockService.submit(dto.getIds());
        return  success();
    }

    /**
     * 审核
     * @param baseApproveParamDTO
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:approve",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        initStockService.approve(baseApproveParamDTO);
        return  success();
    }

    /**
     * 反审核
     * @param dto
     * @return
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:disApprove",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult<Void> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        initStockService.disApprove(dto.getIds());
        return  success();
    }

    /**
     * 撤销
     * @param dto
     * @return
     */
    @PostMapping("/cancel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:cancel",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult cancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        initStockService.cancel(dto.getIds());
        return  success();
    }

    /**
     * 作废
     * @param dto
     * @return
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:invalid",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult<Void> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        initStockService.invalid(dto.getIds(), dto.getRemark());
        return success();
    }

    /**
     * 删除
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:delete",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        initStockService.delete(dto.getIds());
        return success();
    }

    /**
     * 导出
     * @param dto
     * @param response
     * @return
     */
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:initStock:paging",
            tableAlias = "ism"
    )
    public ApiResult<Void> exportExcel(@RequestBody InitStockDTO.ExportSearchParamDTO dto, HttpServletResponse response) {
        initStockService.exportExcel(dto, response);
        return null;
    }

    /**
     * 下载模板
     * @param response
     * @return
     */
    @GetMapping("/exportExcelTemplate")
    public ApiResult exportTemplate(HttpServletResponse response) {
        initStockService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入
     * @param file
     * @param response
     * @return
     */
    @PostMapping("/importFile")
    public ApiResult<InitStockDetailDTO.ImportDTO> importFile(@RequestParam("excelFile") MultipartFile file, HttpServletResponse response) {
        return success(initStockService.importFile(file, response));
    }

}