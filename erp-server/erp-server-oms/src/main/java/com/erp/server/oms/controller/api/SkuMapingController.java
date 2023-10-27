package com.erp.server.oms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
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
@LogSystemModule("sku对照表")
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
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入sku对照表")
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
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板sku对照表")
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
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出sku对照表")
    @PostMapping("/export")
    public ApiResult exportSkuMaping(@RequestBody @Valid SkuMapingDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = skuMapingService.exportSkuMaping(dto, response);
        return result ? success() : failure();
    }

    /**
     * 更改sku 对照表
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更改sku对照表:id={id},产品sku={productSkuId},平台sku={platformSkuNo}")
    @PostMapping("/updateSkuMaping")
    public ApiResult updateSkuMaping(@RequestBody @Valid SkuMapingDTO.UpdateDTO dto) {
        String id = skuMapingService.updateSkuMaping(dto);
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
    public ApiResult<PagingVO<SkuMapingDTO.ProductSkuInfoDTO>> list(@RequestBody @Validated PagingDTO<SkuMapingDTO.ListParamDTO> dto) {
        PagingVO<SkuMapingDTO.ProductSkuInfoDTO> pagingVO = skuMapingService.listPaging(dto);
        return success(pagingVO);
    }


}
