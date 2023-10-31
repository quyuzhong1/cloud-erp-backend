package com.erp.server.oms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
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
 * SKU对照表管理
 *
 * @author Lambda
 * @since 2023-06-28
 */
@RestController
@LogSystemModule("sku对照表")
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
     * 添加库存对应sku
     * @author yl
     * @date 2023-08-18 16:31
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     */
    @PostMapping("/addWarehouseSku")
    public ApiResult add(@RequestBody @Validated SkuMappingDTO.AddWarehouseSkuDTO dto) {
        String id = skuMappingService.addWarehouseSku(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 平台分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/platformPaging")
    public ApiResult<PagingVO<SkuMappingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SkuMappingDTO.PagingParamDTO> dto) {
        PagingVO<SkuMappingDTO.PagingViewDTO> pagingVO = skuMappingService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 库存SKU 分页
     *
     * @param dto
     * @return
     */
    @PostMapping("/warehousePaging")
    public ApiResult<PagingVO<SkuMappingDTO.WarehousePagingViewDTO>> queryWarehouseByPage(@RequestBody @Validated PagingDTO<SkuMappingDTO.WarehousePagingParamDTO> dto) {
        PagingVO<SkuMappingDTO.WarehousePagingViewDTO> pagingVO = skuMappingService.warehousePaging(dto);
        return success(pagingVO);
    }


    /**
     * 导入平台sku对照表
     *
     * @return
     */
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "type") String type,HttpServletResponse response) {
        Boolean result = skuMappingService.importExcel(excelFile,type, response);
        return result ? success() : failure();
    }


    /**
     * 下载平台sku对照模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板sku对照表")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(@RequestParam(value = "importType") String type, HttpServletResponse response) {
        skuMappingService.downloadTemplate(type,response);
        return success();
    }



    /**
     * 导出平台sku 对照表
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出sku对照表")
    @PostMapping("/exportPlatformSku")
    public ApiResult exportPlatformSku(@RequestBody @Valid SkuMappingDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = skuMappingService.exportPlatformSku(dto, response);
        return result ? success() : failure();
    }

    /**
     * 导出库存sku 对照表
     *
     * @return
     */
    @PostMapping("/exportWarehouseSku")
    public ApiResult exportWarehouseSku(@RequestBody @Valid SkuMappingDTO.ExportWarehouseSkuDTO dto, HttpServletResponse response) {
        Boolean result = skuMappingService.exportWarehouseSku(dto, response);
        return result ? success() : failure();
    }

    /**
     * 更改库存sku 对照表
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更改sku对照表:id={id},产品sku={productSkuId}")
    @PostMapping("/updateWarehouseSku")
    public ApiResult updateWarehouseSku(@RequestBody @Valid SkuMappingDTO.UpdateWarehouseSkuDTO dto) {
        String id = skuMappingService.updateWarehouseSku(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 更改平台 对照表
     *
     * @return
     */
    @PostMapping("/updatePlatformSku")
    public ApiResult updatePlatformSku(@RequestBody @Valid SkuMappingDTO.UpdatePlatformDTO dto) {
        String id = skuMappingService.updatePlatformSku(dto);
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


    /**
     * 根据sku集合查询库存sku
     * @author Will
     * @date: 2023/8/24 19:13
     * @param list
     * @return ApiResult<List<ListSkuDTO>>
     */
    @PostMapping("/listBySkuNoList")
    public ApiResult<List<SkuMappingDTO.ListSkuDTO>> listBySkuNoList(@RequestBody @Validated ValidList<SkuMappingDTO.ListSkuParamDTO> list) {
        List<SkuMappingDTO.ListSkuDTO> resultList = skuMappingService.listBySkuNoList(list);
        return success(resultList);
    }
}
