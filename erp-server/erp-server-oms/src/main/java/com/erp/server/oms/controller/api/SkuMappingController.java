package com.erp.server.oms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.server.oms.service.SkuMappingService;
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
public class SkuMappingController extends BaseController {


    @Resource
    private SkuMappingService skuMappingService;


    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<SkuMappingDTO.TabListDTO>> tabList(@Validated @RequestBody SkuMappingDTO.FindTabDTO dto) {
        List<SkuMappingDTO.TabListDTO> list = skuMappingService.tabList(dto);
        return success(list);
    }


    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SkuMappingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SkuMappingDTO.PagingParamDTO> dto) {
        PagingVO<SkuMappingDTO.PagingViewDTO> pagingVO = skuMappingService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 导入sku 对照表
     *
     * @return
     */
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = skuMappingService.importExcel(excelFile, response);
        return result ? success() : failure();
    }


    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        skuMappingService.downloadTemplate(response);
        return success();
    }

    /**
     * 导出 sku 对照表
     *
     * @return
     */
    @PostMapping("/export")
    public ApiResult exportSkuMaping(@RequestBody @Valid SkuMappingDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = skuMappingService.exportSkuMaping(dto, response);
        return result ? success() : failure();
    }

    /**
     * 更改sku 对照表
     *
     * @return
     */
    @PostMapping("/updateSkuMaping")
    public ApiResult updateSkuMaping(@RequestBody @Valid SkuMappingDTO.UpdateDTO dto) {
        String id = skuMappingService.updateSkuMaping(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }
 
   /**
    * 对应销售订单 添加客户sku
    * @author yl
    * @date 2023-07-01 9:13
    * @param dto
    * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.ProductSkuInfoDTO>>
    */
    @PostMapping("/list")
    public ApiResult<PagingVO<SkuMappingDTO.ProductSkuInfoDTO>> list(@RequestBody @Validated PagingDTO<SkuMappingDTO.ListParamDTO> dto) {
        PagingVO<SkuMappingDTO.ProductSkuInfoDTO> pagingVO = skuMappingService.listPaging(dto);
        return success(pagingVO);
    }


}
