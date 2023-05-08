package com.erp.server.wms.controller.api;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Classname: InitStockController
 * @Description: 期初库存控制器
 * @CreateTime: 2023-05-08  17:43
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/initStock")
public class InitStockController extends BaseController {

    // TODO 加权限

    /**
     * 分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<InitStockDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<InitStockDTO.SearchParamDTO> dto) {
        return success(null);
    }

    /**
     * 新增
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody @Validated InitStockDTO.AddDTO dto) {
        return  success();
    }

    /**
     * 修改
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult<Void> update(@RequestBody @Validated InitStockDTO.UpdateDTO dto) {
        return  success();
    }

    /**
     * 新增并提交
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated InitStockDTO.AddDTO dto) {
        return  success();
    }

    /**
     * 修改并提交
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated InitStockDTO.UpdateDTO dto) {
        return  success();
    }


    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/view")
    public ApiResult<InitStockDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(null);
    }

    /**
     * 提交审核
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return  success();
    }

    /**
     * 审核
     * @param baseApproveParamDTO
     * @return
     */
    @PostMapping("/approve")
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        return  success();
    }

    /**
     * 反审核
     * @param dto
     * @return
     */
    @PostMapping("/disApprove")
    public ApiResult<Void> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return  success();
    }

    /**
     * 撤销
     * @param dto
     * @return
     */
    @PostMapping("/cancel")
    public ApiResult cancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return  success();
    }

    /**
     * 作废
     * @param dto
     * @return
     */
    @PostMapping("/invalid")
    public ApiResult<Void> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        return success();
    }

    /**
     * 删除
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success();
    }

    /**
     * 导出
     * @param dto
     * @param response
     * @return
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult<Void> exportExcel(@RequestBody InitStockDTO.SearchParamDTO dto, HttpServletResponse response) {
        return success();
    }

    /**
     * 下载模板
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/exportExcelTemplate")
    public ApiResult<Void> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        return success();
    }

    /**
     * 导入
     * @param excelImportDTO
     * @param response
     * @return
     */
    @PostMapping("/importFile")
    public ApiResult<InitStockDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated InitStockDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        return success();
    }

}