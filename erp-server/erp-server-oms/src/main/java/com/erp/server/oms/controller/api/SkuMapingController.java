package com.erp.server.oms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SkuMapingDTO;
import com.erp.server.oms.service.SkuMapingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * sku对照
 *
 * @author Lambda
 * @since 2023-06-28
 */
@RestController
@RequestMapping("/skuMaping")
public class SkuMapingController extends BaseController {


    @Resource
    private SkuMapingService skuMapingService;



    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<SkuMapingDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<SkuMapingDTO.TabListDTO> list = skuMapingService.tabList(dto);
        return success(list);
    }


    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SkuMapingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SkuMapingDTO.PagingParamDTO> dto) {
        PagingVO<SkuMapingDTO.PagingViewDTO> pagingVO = skuMapingService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 导入sku 对照表
     *
     * @return
     */
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = skuMapingService.importExcel(excelFile, response);
        return result ? success() : failure();
    }


    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        skuMapingService.downloadTemplate(response);
        return success();
    }

    /**
     * 导出 sku 对照表
     *
     * @return
     */
    @PostMapping("/export")
    public ApiResult exportSkuMaping(@RequestBody @Valid SkuMapingDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = skuMapingService.exportSkuMaping(dto,response);
        return result ? success() : failure();
    }

    /**
     * 更改sku 对照表
     *
     * @return
     */
    @PostMapping("/updateSkuMaping")
    public ApiResult updateSkuMaping(@RequestBody @Valid SkuMapingDTO.UpdateDTO dto) {
        String id = skuMapingService.updateSkuMaping(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


}
