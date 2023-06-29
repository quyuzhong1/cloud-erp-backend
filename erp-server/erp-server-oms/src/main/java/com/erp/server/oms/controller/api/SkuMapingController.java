package com.erp.server.oms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SkuMapingDTO;
import com.erp.server.oms.service.SkuMapingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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
        Boolean result = skuMapingService.importExcel(excelFile,response);
        return result?success():failure();
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


}
