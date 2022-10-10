package com.erp.server.plm.controller;

import com.alibaba.excel.EasyExcel;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.server.plm.listener.ProductDetailExcelListener;
import com.erp.server.plm.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * 产品管理
 * @Author Luo_WG
 * @Date 2022/9/22 11:48
 **/
@Api(tags = "产品管理")
@RestController
@RequestMapping("plm/product/detail")
public class ProductDetailController extends BaseController {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductCostService productCostService;

    @Resource
    private ProductPurchaseService productPurchaseService;

    @Resource
    private ProductPurchaseRemarkService productPurchaseRemarkService;

    @Resource
    private ProductSaleService productSaleService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private ProductPackService productPackService;

    @Resource
    private ProductCertificateService productCertificateService;

    @Resource
    private ProductVariantService productVariantService;

    @Resource
    private ProductVariantPropertyService productVariantPropertyService;

    @Resource
    private ProductImagesService productImagesService;

    @Resource
    private ProductUnitService productUnitService;

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private BasicDictService basicDictService;

    /**
     * 产品信息-主页列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:15
     * @param pagingDTO pagingDTO
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.PagingVO<com.erp.model.plm.dto.ProductDetailShowDTO>>
     **/
    @ApiOperation(value = "产品信息-主页列表-查询")
    @PostMapping("/list")
    public ApiResult<PagingVO<ProductDetailShowDTO>> list(@RequestBody PagingDTO<ProductSkuDTO> pagingDTO) {
        PagingVO<ProductDetailShowDTO> paging = productDetailService.paging(pagingDTO);
        return this.success(paging);
    }

    /**
     * 产品信息-无规格-产品详情
     * @Author Luo_WG
     * @Date 2022/10/9 10:21
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<com.erp.model.plm.dto.ProductNoSpecDetailAllDTO>
     **/
    @ApiOperation(value = "产品信息-无规格-产品详情")
    @GetMapping("/getNoSpecDetailById")
    @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true)
    public ApiResult<ProductNoSpecDetailAllDTO> getNoSpecDetailById(@RequestParam(value = "productId") String productId) {
        ProductNoSpecDetailAllDTO list = productDetailService.getNoSpecDetailById(productId);
        return this.success(list);
    }

    /**
     * 产品信息-多规格-产品详情
     * @Author Luo_WG
     * @Date 2022/10/9 10:22
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<com.erp.model.plm.dto.ProductManyDetailDTO>
     **/
    @ApiOperation(value = "产品信息-多规格-产品详情")
    @GetMapping("/getManySpecDetailById")
    @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true)
    public ApiResult<ProductManyDetailDTO> getManySpecDetailById(@RequestParam(value = "productId") String productId) {
        ProductManyDetailDTO list = productDetailService.getManySpecDetailById(productId);
        return this.success(list);
    }

    /**
     * 产品信息-无规格-新增/修改
     * @Author Luo_WG
     * @Date 2022/10/9 10:22
     * @param productNoSpecDTO 新增产品无规格sku信息请求参数
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-无规格-新增/修改")
    @PostMapping("/saveOrUpdateNoSpec")
    public ApiResult saveOrUpdateNoSpec(@RequestBody ProductNoSpecDTO productNoSpecDTO) {
        Boolean flag = productDetailService.saveOrUpdateNoSpec(productNoSpecDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-多规格-新增/修改
     * @Author Luo_WG
     * @Date 2022/10/9 10:23
     * @param productManySpecDTO 新增产品多规格sku信息请求参数
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-多规格-新增/修改")
    @PostMapping("/saveOrUpdateManySpec")
    public ApiResult saveOrUpdateManySpec(@RequestBody ProductManySpecDTO productManySpecDTO) {
        Boolean flag = productDetailService.saveOrUpdateManySpec(productManySpecDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-无规格-基础信息上传图片
     * @Author Luo_WG
     * @Date 2022/10/9 10:23
     * @param productImagesDTO 产品图片信息请求参数
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-无规格-基础信息上传图片")
    @PostMapping("/insertProductImage")
    public ApiResult insertProductImage(@RequestBody ProductImagesDTO productImagesDTO) {
        Boolean flag = productImagesService.insertProductImage(productImagesDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-多规格-自动生成
     * @Author Luo_WG
     * @Date 2022/10/9 10:23
     * @param variantAutoAddDTO 商品管理-产品信息-多规格-自动生成 请求参数
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.entity.ProductDetailEntity>>
     **/
    @ApiOperation(value = "产品信息-多规格-自动生成")
    @PostMapping("/InsertManySpecSku")
    public ApiResult<List<ProductDetailEntity>> InsertManySpecAuto(@RequestBody VariantAutoAddDTO variantAutoAddDTO) {
        List<ProductDetailEntity> list = productDetailService.insertManySpecAuto(variantAutoAddDTO);
        return this.success(list);
    }

    /**
     * 产品信息-多规格sku-删除
     * @Author Luo_WG
     * @Date 2022/10/9 10:42
     * @param skuId sku表id
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-多规格sku-删除")
    @PostMapping("/delete")
    public ApiResult delete(@RequestParam(value = "skuId")  String skuId) {
        Boolean flag = productDetailService.delete(skuId);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 成本信息-主页列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:24
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>>
     **/
    @ApiOperation(value = "成本信息-主页列表-查询")
    @GetMapping("/listCost")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductCostShowDTO>> listCost(@RequestParam(value = "productId") String productId) {
        List<ProductCostShowDTO> list = productCostService.list(productId);
        return this.success(list);
    }

    /**
     * 采购信息-主页列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:24
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.dto.ProductPurchaseShowDTO>>
     **/
    @ApiOperation(value = "采购信息-主页列表-查询")
    @GetMapping("/listProductPurchase")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductPurchaseShowDTO>> listPurchase(@RequestParam(value = "productId") String productId) {
        List<ProductPurchaseShowDTO> list = productPurchaseService.list(productId);
        return this.success(list);
    }

    /**
     * 销售信息-主页列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:24
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.dto.ProductSaleShowDTO>>
     **/
    @ApiOperation(value = "销售信息-主页列表-查询")
    @GetMapping("/listSale")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductSaleShowDTO>> listSale(@RequestParam(value = "productId") String productId) {
        List<ProductSaleShowDTO> list = productSaleService.list(productId);
        return this.success(list);
    }

    /**
     * 物流信息-报关信息列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:24
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.dto.ProductLogisticsShowDTO>>
     **/
    @ApiOperation(value = "物流信息-报关信息列表-查询")
    @GetMapping("/listLogistics")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductLogisticsShowDTO>> listLogistics(@RequestParam(value = "productId") String productId) {
        List<ProductLogisticsShowDTO> list = productLogisticsService.list(productId);
        return this.success(list);
    }

    /**
     * 物流信息-包装信息列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:25
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.dto.ProductPackShowDTO>>
     **/
    @ApiOperation(value = "物流信息-包装信息列表-查询")
    @GetMapping("/listPack")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductPackShowDTO>> listPack(@RequestParam(value = "productId") String productId) {
        List<ProductPackShowDTO> list = productPackService.list(productId);
        return this.success(list);
    }

    /**
     * 证书信息-主页列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:25
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.dto.ProductCertificateShowDTO>>
     **/
    @ApiOperation(value = "证书信息-主页列表-查询")
    @GetMapping("/listCertificate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductCertificateShowDTO>> listCertificate(@RequestParam(value = "productId") String productId) {
        List<ProductCertificateShowDTO> list = productCertificateService.list(productId);
        return this.success(list);
    }

    /**
     * 证书信息-主页列表-新增|修改
     * @Author Luo_WG
     * @Date 2022/10/9 10:25
     * @param productCertificateDTO 产品证书表
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "证书信息-主页列表-新增|修改")
    @GetMapping("/saveOrUpdateCertificate")
    public ApiResult saveOrUpdateCertificate(@RequestBody List<ProductCertificateDTO> productCertificateDTO) {
        Boolean flag = productCertificateService.saveOrUpdateBatch(productCertificateDTO);
        return  flag == true ? this.success() : this.failure();
    }

    /**
     * 证书信息-主页列表-删除
     * @Author Luo_WG
     * @Date 2022/10/9 10:25
     * @param id 证书信息id
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "证书信息-主页列表-删除")
    @GetMapping("/removeCertificate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "证书信息id", required = true),
    })
    public ApiResult removeCertificate(@RequestParam("id") String id) {
        Boolean flag = productCertificateService.removeCertificate(id);
        return  flag == true ? this.success() : this.failure();
    }

    /**
     * 采购信息-主页备注信息列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     * @param purchaseId 产品采购信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.entity.ProductPurchaseRemarkEntity>>
     **/
    @ApiOperation(value = "采购信息-主页备注信息列表-查询")
    @GetMapping("/listPurchaseRemark")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "purchaseId", value = "产品采购信息表id", required = true),
    })
    public ApiResult<List<ProductPurchaseRemarkEntity>> listPurchaseRemark(@RequestParam(value = "purchaseId") String purchaseId) {
        List<ProductPurchaseRemarkEntity> list = productPurchaseRemarkService.list(purchaseId);
        return this.success(list);
    }

    /**
     * 采购信息-备注信息-新增
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     * @param dto 产品采购备注信息列表（VO）
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "采购信息-备注信息-新增")
    @PostMapping("/saveOrUpdatePurchaseRemark")
    public ApiResult saveOrUpdatePurchaseRemark(@RequestBody ProductPurchaseRemarkDTO dto) {
        Boolean flag = productPurchaseRemarkService.saveOrUpdate(dto);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-变体管理-下拉列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.entity.ProductVariantEntity>>
     **/
    @ApiOperation(value = "产品信息-变体管理-下拉列表-查询")
    @GetMapping("/listVariant")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductVariantEntity>> listVariant(@RequestParam(value = "productId") String productId) {
        List<ProductVariantEntity> list = productVariantService.list(productId);
        return this.success(list);
    }

    /**
     * 产品信息-变体管理-下拉列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.entity.ProductVariantEntity>>
     **/
    @ApiOperation(value = "产品信息-变体管理-编辑-查询")
    @GetMapping("/listVariantAndProperty")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductVariantEntity>> listVariantAndProperty(@RequestParam(value = "productId") String productId) {
        List<ProductVariantEntity> list = productVariantService.list(productId);
        return this.success(list);
    }

    /**
     * 产品信息-变体管理-下拉列表-新增/修改
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     * @param productVariantDTO 产品变体类型属性表
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-变体管理-下拉列表-新增/修改")
    @PostMapping("/saveOrUpdateVariant")
    public ApiResult saveOrUpdateVariant(@RequestBody ProductVariantDTO productVariantDTO) {
        Boolean flag = productVariantService.saveOrUpdate(productVariantDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-变体管理-下拉列表-删除
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     * @param variantId 变体类型表主键Id
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-变体管理-下拉列表-删除")
    @PostMapping("/deleteVariant")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "variantId", value = "变体类型表主键Id", required = true),
    })
    public ApiResult deleteVariant(@RequestParam(value="variantId") String variantId){
        Boolean flag = productVariantService.deleteVariant(variantId);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-变体管理-变体值-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     * @param variantId 变体类型表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.entity.ProductVariantPropertyEntity>>
     **/
    @ApiOperation(value = "产品信息-变体管理-变体值-查询")
    @GetMapping("/listVariantProperty")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "variantId", value = "变体类型表id", required = true),
    })
    public ApiResult<List<ProductVariantPropertyEntity>> listVariantProperty(@RequestParam(value = "variantId") String variantId) {
        List<ProductVariantPropertyEntity> list = productVariantPropertyService.list(variantId);
        return this.success(list);
    }

    /**
     * 产品信息-变体管理-变体值-新增/修改
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     * @param productVariantPropertyDTO 产品变体属性值表
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-变体管理-变体值-新增/修改")
    @PostMapping("/saveOrUpdateVariantProperty")
    public ApiResult saveOrUpdateVariantProperty(@RequestBody ProductVariantPropertyDTO productVariantPropertyDTO) {
        Boolean flag = productVariantPropertyService.saveOrUpdate(productVariantPropertyDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-变体管理-变体值-删除
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     * @param variantPropertyId 变体值表id
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-变体管理-变体值-删除")
    @GetMapping("/deleteVariantProperty")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "variantPropertyId", value = "变体值表id", required = true),
    })
    public ApiResult deleteVariantProperty(@RequestBody String variantPropertyId) {
        Boolean flag = productVariantPropertyService.deleteVariant(variantPropertyId);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-单位管理-新增|修改
     * @Author Luo_WG
     * @Date 2022/10/9 10:28
     * @param productUnitList productUnitList
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-单位管理-新增|修改")
    @PostMapping("/saveOrUpdateProductUnit")
    public ApiResult saveOrUpdateProductUnit(@RequestBody @Validated List<ProductUnitDTO> productUnitList) {
        Boolean flag = productUnitService.saveOrUpdateBatch(productUnitList);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-单位管理-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:28
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.entity.ProductUnitEntity>>
     **/
    @ApiOperation(value = "产品信息-单位管理-查询")
    @GetMapping("/listProductUnit")
    public ApiResult<List<ProductUnitEntity>> listProductUnit() {
        List<ProductUnitEntity> list = productUnitService.list();
        return this.success(list);
    }

    /**
     * 产品信息-单位管理-删除
     * @Author Luo_WG
     * @Date 2022/10/9 10:28
     * @param id 单位列表id
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "产品信息-单位管理-删除")
    @PostMapping("/deleteProductUnit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "单位列表id", required = true),
    })
    public ApiResult deleteProductUnit(String id) {
        Boolean flag = productUnitService.delete(id);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * excel导入产品信息
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     * @param excelFile 文件流
     * @param importType 请求类型
     * @param response 响应
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "Excel导入产品信息")
    @PostMapping("/importProductFile")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "excelFile", value = "文件流", required = true),
            @ApiImplicitParam(name = "importType", value = "请求类型 1：导入新增  2：导入修改", required = true),
    })
    public ApiResult importProductFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "importType") Integer importType, HttpServletResponse response) throws Exception{
        ProductDetailExcelListener excelListenerUtil = new ProductDetailExcelListener(importType, productDetailService, productInfoService, basicCategoryService, basicDictService);
        EasyExcel.read(excelFile.getInputStream(), ProductDetailExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        List<ProductDetailExcelDTO> list = excelListenerUtil.getDateList();
        if(list.size() > 0){
            StringBuffer sb = new StringBuffer();
            String fileName = URLEncoder.encode("产品管理", "UTF-8");
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(fileName);
            sb.append(date);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            ExcelUtil.export(sb.toString(), "商品列表", list, ProductDetailExcelDTO.class, response);

            /*response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("测试", "UTF-8");
            String s = new String("测试".getBytes("UTF-8"), "ISO-8859-1");
            response.setHeader("Content-disposition", "attachment;filename=" + s + ".xlsx");
            EasyExcel.write(response.getOutputStream(), ProductDetailExcelDTO.class).sheet().doWrite(list);*/
        }
        return this.success();
    }

    /**
     * excel导出产品信息
     * @Author Luo_WG
     * @Date 2022/10/9 11:49
     * @param productSkuDTO productSkuDTO
     * @param response response
     * @return com.erp.common.dto.base.ApiResult
     **/
    @ApiOperation(value = "excel导出产品信息")
    @PostMapping(value = "/exportProduct", produces = "application/octet-stream")
    public void exportProduct(@RequestBody ProductSkuDTO productSkuDTO, HttpServletResponse response) {
        productDetailService.exportProduct(productSkuDTO, response);
    }
}
