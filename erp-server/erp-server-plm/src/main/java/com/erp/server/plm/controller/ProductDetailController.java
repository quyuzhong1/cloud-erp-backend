package com.erp.server.plm.controller;

import com.alibaba.excel.EasyExcel;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.ProductDetailExcelListener;
import com.erp.server.plm.service.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * 产品管理
 *
 * @Author Luo_WG
 * @Date 2022/9/22 11:48
 **/
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

    @Resource
    private SysUserFeign sysUserFeign;


    /**
     * 产品信息-主页列表-查询1
     *
     * @param pagingDTO pagingDTO
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.PagingVO < com.erp.model.plm.dto.ProductDetailShowDTO>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:15
     **/
    @PostMapping("/list")
    //@RequestPermissions("plm:product:detail:list")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:detail:list", tableAlias = "pd")
    public ApiResult<PagingVO<ProductDetailShowDTO>> list(@RequestBody PagingDTO<ProductSkuDTO> pagingDTO) {
        PagingVO<ProductDetailShowDTO> paging = productDetailService.paging(pagingDTO);
        return this.success(paging);
    }

    /**
     * 产品信息-无规格-产品详情
     *
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<com.erp.model.plm.dto.ProductNoSpecDetailAllDTO>
     * @Author Luo_WG
     * @Date 2022/10/9 10:21
     **/
    @GetMapping("/getNoSpecDetailById")
    //@RequestPermissions("plm:product:detail:getNoSpecDetailById")
    public ApiResult<ProductNoSpecDetailAllDTO> getNoSpecDetailById(@RequestParam(value = "productId") String productId) {
        ProductNoSpecDetailAllDTO list = productDetailService.getNoSpecDetailById(productId);
        return this.success(list);
    }

    /**
     * 产品信息-多规格-产品详情
     *
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<com.erp.model.plm.dto.ProductManyDetailDTO>
     * @Author Luo_WG
     * @Date 2022/10/9 10:22
     **/
    @GetMapping("/getManySpecDetailById")
    //@RequestPermissions("plm:product:detail:getManySpecDetailById")
    public ApiResult<ProductManyDetailDTO> getManySpecDetailById(@RequestParam(value = "productId") String productId) {
        ProductManyDetailDTO list = productDetailService.getManySpecDetailById(productId);
        return this.success(list);
    }


    /**
     * 产品信息-多规格-产品详情-编辑
     *
     * @param dto 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<com.erp.model.plm.dto.ProductManyDetailDTO>
     * @Author Luo_WG
     * @Date 2022/10/9 10:22
     **/
    @PostMapping("/getManySpecDetailByIdUpdate")
    //@RequestPermissions("plm:product:detail:getManySpecDetailById")
/*    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:product:detail:edit",
            serviceClass = ProductDetailService.class,
            keyIdName = "id"
    )*/
    public ApiResult<ProductManyDetailDTO> getManySpecDetailByIdUpdate(@RequestBody ProductManySpecUpdateDTO dto) {
        ProductManyDetailDTO list = productDetailService.getManySpecDetailById(dto.getProductId());
        return this.success(list);
    }

    /**
     * 产品信息-无规格-产品详情-编辑
     *
     * @param dto 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<com.erp.model.plm.dto.ProductNoSpecDetailAllDTO>
     * @Author Luo_WG
     * @Date 2022/10/9 10:21
     **/
    @PostMapping("/getNoSpecDetailByIdUpdate")
    //@RequestPermissions("plm:product:detail:getManySpecDetailById")
/*    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:product:detail:edit",
            serviceClass = ProductDetailService.class,
            keyIdName = "id"
    )*/
    //@RequestPermissions("plm:product:detail:getNoSpecDetailById")
    public ApiResult<ProductNoSpecDetailAllDTO> getNoSpecDetailById(@RequestBody ProductManySpecUpdateDTO dto) {
        ProductNoSpecDetailAllDTO list = productDetailService.getNoSpecDetailById(dto.getProductId());
        return this.success(list);
    }

    /**
     * 产品信息-无规格-新增/修改
     *
     * @param productNoSpecDTO 新增产品无规格sku信息请求参数
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:22
     **/
    @PostMapping("/saveOrUpdateNoSpec")
    //@RequestPermissions("plm:product:detail:saveOrUpdateNoSpec")
    public ApiResult saveOrUpdateNoSpec(@RequestBody @Validated ProductNoSpecDTO productNoSpecDTO) {
        Boolean flag = productDetailService.saveOrUpdateNoSpec(productNoSpecDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-多规格-新增/修改
     *
     * @param productManySpecDTO 新增产品多规格sku信息请求参数
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:23
     **/
    @PostMapping("/saveOrUpdateManySpec")
    //@RequestPermissions("plm:product:detail:saveOrUpdateManySpec")
    public ApiResult saveOrUpdateManySpec(@RequestBody @Validated ProductManySpecDTO productManySpecDTO) {
        Boolean flag = productDetailService.saveOrUpdateManySpec(productManySpecDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-无规格-基础信息上传图片
     *
     * @param productImagesDTO 产品图片信息请求参数
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:23
     **/
    @PostMapping("/insertProductImage")
    //@RequestPermissions("plm:product:detail:insertProductImage")
    public ApiResult insertProductImage(@RequestBody ProductImagesDTO productImagesDTO) {
        Boolean flag = productImagesService.insertProductImage(productImagesDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-多规格-自动生成
     *
     * @param variantAutoAddDTO 商品管理-产品信息-多规格-自动生成 请求参数
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.entity.ProductDetailEntity>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:23
     **/
    @PostMapping("/InsertManySpecSku")
    //@RequestPermissions("plm:product:detail:InsertManySpecAuto")
    public ApiResult<List<ProductDetailEntity>> InsertManySpecAuto(@RequestBody VariantAutoAddDTO variantAutoAddDTO) {
        List<ProductDetailEntity> list = productDetailService.insertManySpecAuto(variantAutoAddDTO);
        return this.success(list);
    }

    /**
     * 产品信息-多规格sku-删除
     *
     * @param skuId sku表id
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:42
     **/
    @PostMapping("/delete")
    //@RequestPermissions("plm:product:detail:delete")
    //@DataPermission(operationType = "delete", tableField = "create_user_id", menuCode = "plm:product:detail:delete", serviceClass = ProductDetailServiceImpl.class)
/*    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_PARAM,
            tableField = "charge_id",
            menuCode = "plm:product:detail:delete",
            serviceClass = ProductDetailService.class)*/
    public ApiResult delete(@RequestParam(value = "skuId") String skuId) {
        Boolean flag = productDetailService.delete(skuId);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-取消按钮-删除
     *
     * @param id spu主表id
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:42
     **/
    @PostMapping("/deleteByProductId")
    //@RequestPermissions("plm:product:detail:delete")
    //@DataPermission(operationType = "deleteProduct", tableField = "create_user_id", menuCode = "plm:product:detail:delete", serviceClass = ProductDetailServiceImpl.class)
    public ApiResult deleteByProductId(@RequestParam(value = "id") String id) {
        Boolean flag = productDetailService.deleteByProductId(id);
        return flag == true ? this.success() : this.failure();
    }

    /*    *//**
     * 产品信息-多规格sku-批量删除
     * @Author Luo_WG
     * @Date 2022/10/9 10:42
     * @param skuIds sku表id
     * @return com.erp.common.dto.base.ApiResult
     **//*
    @PostMapping("/deleteBatch")
    @RequestPermissions("plm:product:detail:deleteBatch")
    public ApiResult deleteBatch(@RequestParam(value = "skuIds")  List<String> skuIds) {
        Boolean flag = productDetailService.deleteBatch(skuIds);
        return flag == true ? this.success() : this.failure();
    }*/

    /**
     * 成本信息-主页列表-查询
     *
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.dto.ProductCostShowDTO>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:24
     **/
    @GetMapping("/listCost")
    public ApiResult<List<ProductCostShowDTO>> listCost(@RequestParam(value = "productId") String productId) {
        List<ProductCostShowDTO> list = productCostService.list(productId);
        return this.success(list);
    }

    /**
     * 采购信息-主页列表-查询
     *
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.dto.ProductPurchaseShowDTO>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:24
     **/
    @GetMapping("/listProductPurchase")
    public ApiResult<List<ProductPurchaseShowDTO>> listPurchase(@RequestParam(value = "productId") String productId) {
        List<ProductPurchaseShowDTO> list = productPurchaseService.list(productId);
        return this.success(list);
    }

    /**
     * 销售信息-主页列表-查询
     *
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.dto.ProductSaleShowDTO>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:24
     **/
    @GetMapping("/listSale")
    public ApiResult<List<ProductSaleShowDTO>> listSale(@RequestParam(value = "productId") String productId) {
        List<ProductSaleShowDTO> list = productSaleService.list(productId);
        return this.success(list);
    }

    /**
     * 物流信息-报关信息列表-查询
     *
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.dto.ProductLogisticsShowDTO>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:24
     **/
    @GetMapping("/listLogistics")
    public ApiResult<List<ProductLogisticsShowDTO>> listLogistics(@RequestParam(value = "productId") String productId) {
        List<ProductLogisticsShowDTO> list = productLogisticsService.list(productId);
        return this.success(list);
    }

    /**
     * 物流信息-包装信息列表-查询
     *
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.dto.ProductPackShowDTO>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:25
     **/
    @GetMapping("/listPack")
    public ApiResult<List<ProductPackShowDTO>> listPack(@RequestParam(value = "productId") String productId) {
        List<ProductPackShowDTO> list = productPackService.list(productId);
        return this.success(list);
    }

    /**
     * 证书信息-主页列表-查询
     *
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.dto.ProductCertificateShowDTO>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:25
     **/
    @GetMapping("/listCertificate")
    public ApiResult<List<ProductCertificateShowDTO>> listCertificate(@RequestParam(value = "productId") String productId) {
        List<ProductCertificateShowDTO> list = productCertificateService.list(productId);
        return this.success(list);
    }

    /*    *//**
     * 证书信息-主页列表-新增|修改
     * @Author Luo_WG
     * @Date 2022/10/9 10:25
     * @param productCertificateDTO 产品证书表
     * @return com.erp.common.dto.base.ApiResult
     **//*
    @GetMapping("/saveOrUpdateCertificate")
    public ApiResult saveOrUpdateCertificate(@RequestBody List<ProductCertificateDTO> productCertificateDTO) {
        Boolean flag = productCertificateService.saveOrUpdateBatch(productCertificateDTO);
        return  flag == true ? this.success() : this.failure();
    }*/

    /**
     * 证书信息-主页列表-删除
     *
     * @param id 证书信息id
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:25
     **/
    @GetMapping("/removeCertificate")
    //@RequestPermissions("plm:product:detail:removeCertificate")
    public ApiResult removeCertificateById(@RequestParam("id") String id) {
        Boolean flag = productCertificateService.removeById(id);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 采购信息-主页备注信息列表-查询
     *
     * @param productId:产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.entity.ProductPurchaseRemarkEntity>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     **/
    @GetMapping("/listPurchaseRemark")
    //@RequestPermissions("plm:product:detail:listPurchaseRemark")
    public ApiResult<List<ProductPurchaseRemarkEntity>> listPurchaseRemark(@RequestParam(value = "productId") String productId) {
        List<ProductPurchaseRemarkEntity> list = productPurchaseRemarkService.list(productId);
        return this.success(list);
    }

    /**
     * 采购信息-备注信息-新增
     *
     * @param dto 产品采购备注信息列表（VO）
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     **/
    @PostMapping("/saveOrUpdatePurchaseRemark")
    //@RequestPermissions("plm:product:detail:saveOrUpdatePurchaseRemark")
    public ApiResult saveOrUpdatePurchaseRemark(@RequestBody ProductPurchaseRemarkDTO dto) {
        Boolean flag = productPurchaseRemarkService.saveOrUpdate(dto);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 采购信息-备注信息-新增-批量
     *
     * @param dto 产品采购备注信息列表（VO）
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     **/
    @PostMapping("/saveOrUpdatePurchaseRemarkBatch")
    //@RequestPermissions("plm:product:detail:saveOrUpdatePurchaseRemarkBatch")
    public ApiResult saveOrUpdatePurchaseRemarkBatch(@RequestBody List<ProductPurchaseRemarkDTO> dto) {
        Boolean flag = productPurchaseRemarkService.saveOrUpdateBatch(dto);
        return flag == true ? this.success() : this.failure();
    }

    /*    *//**
     * 产品信息-变体管理-下拉列表-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     * @param productId 产品信息表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.entity.ProductVariantEntity>>
     **//*
    @GetMapping("/listVariant")
    public ApiResult<List<ProductVariantEntity>> listVariant() {
        List<ProductVariantEntity> list = productVariantService.list();
        return this.success(list);
    }*/

    /**
     * 产品信息-变体管理-下拉列表-查询
     *
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.entity.ProductVariantEntity>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     **/
    @GetMapping("/listVariantAndProperty")
    //@RequestPermissions("plm:product:detail:listVariantAndProperty")
    public ApiResult<List<ProductVariantDTO>> listVariantAndProperty() {
        List<ProductVariantDTO> productVariantDTOS = productVariantService.listVariantAndProperty();
        return this.success(productVariantDTOS);
    }

    /**
     * 产品信息-变体管理-下拉列表-新增/修改
     *
     * @param productVariantDTO 产品变体类型属性表
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     **/
    @PostMapping("/saveOrUpdateVariant")
    //@RequestPermissions("plm:product:detail:saveOrUpdateVariant")
    public ApiResult saveOrUpdateVariant(@RequestBody ProductVariantDTO productVariantDTO) {
        Boolean flag = productVariantService.saveOrUpdate(productVariantDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-变体管理-下拉列表-删除
     *
     * @param variantId 变体类型表主键Id
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     **/
    @PostMapping("/deleteVariant")
    //@RequestPermissions("plm:product:detail:deleteVariant")
    public ApiResult deleteVariant(@RequestParam(value = "variantId") String variantId) {
        Boolean flag = productVariantService.deleteVariant(variantId);
        return flag == true ? this.success() : this.failure();
    }

    /*    *//**
     * 产品信息-变体管理-变体值-查询
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     * @param variantId 变体类型表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.entity.ProductVariantPropertyEntity>>
     **//*
    @GetMapping("/listVariantProperty")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "variantId", value = "变体类型表id", required = true),
    })
    public ApiResult<List<ProductVariantPropertyEntity>> listVariantProperty(@RequestParam(value = "variantId") String variantId) {
        List<ProductVariantPropertyEntity> list = productVariantPropertyService.list(variantId);
        return this.success(list);
    }*/

    /*    *//**
     * 产品信息-变体管理-变体值-新增/修改
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     * @param productVariantPropertyDTO 产品变体属性值表
     * @return com.erp.common.dto.base.ApiResult
     **//*
    @PostMapping("/saveOrUpdateVariantProperty")
    public ApiResult saveOrUpdateVariantProperty(@RequestBody ProductVariantPropertyDTO productVariantPropertyDTO) {
        Boolean flag = productVariantPropertyService.saveOrUpdate(productVariantPropertyDTO);
        return flag == true ? this.success() : this.failure();
    }*/

    /**
     * 产品信息-变体管理-变体值-删除
     *
     * @param variantPropertyId 变体值表id
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     **/
    @GetMapping("/deleteVariantProperty")
    public ApiResult deleteVariantProperty(@RequestParam(value = "variantPropertyId") String variantPropertyId) {
        Boolean flag = productVariantPropertyService.deleteVariant(variantPropertyId);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-单位管理-新增|修改
     *
     * @param productUnitList productUnitList
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:28
     **/
    @PostMapping("/saveOrUpdateProductUnit")
    //@RequestPermissions("plm:product:detail:saveOrUpdateProductUnit")
    public ApiResult saveOrUpdateProductUnit(@RequestBody @Validated List<ProductUnitDTO> productUnitList) {
        Boolean flag = productUnitService.saveOrUpdateBatch(productUnitList);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-单位管理-查询
     *
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.entity.ProductUnitEntity>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:28
     **/
    @GetMapping("/listProductUnit")
    public ApiResult<List<ProductUnitEntity>> listProductUnit() {
        List<ProductUnitEntity> list = productUnitService.listProductUnit();
        return this.success(list);
    }

    /**
     * 产品信息-单位管理-删除
     *
     * @param id 单位列表id
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:28
     **/
    @PostMapping("/deleteProductUnit")
    //@RequestPermissions("plm:product:detail:deleteProductUnit")
    public ApiResult deleteProductUnit(String id) {
        Boolean flag = productUnitService.delete(id);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * excel导入产品信息
     *
     * @param excelFile  文件流
     * @param importType 请求类型
     * @param response   响应
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @PostMapping("/importProductFile")
    //@RequestPermissions("plm:product:detail:importProductFile")
    public void importProductFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "importType") Integer importType, HttpServletResponse response) {
        ProductDetailExcelListener excelListenerUtil = new ProductDetailExcelListener(importType, productDetailService, productUnitService, basicCategoryService, basicDictService, sysUserFeign);
        try {
            EasyExcel.read(excelFile.getInputStream(), ProductDetailExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<ProductDetailExcelDTO> list = excelListenerUtil.getDateList();
            if (list.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/productNoSpecDetail.xlsx";
                String name = "productNoSpecDetail";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);

                /*response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                response.setCharacterEncoding("utf-8");
                String fileName = URLEncoder.encode("测试", "UTF-8");
                String s = new String("测试".getBytes("UTF-8"), "ISO-8859-1");
                response.setHeader("Content-disposition", "attachment;filename=" + s + ".xlsx");
                EasyExcel.write(response.getOutputStream(), ProductDetailExcelDTO.class).sheet().doWrite(list);*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载导出模板
     *
     * @param request  request
     * @param response response
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @GetMapping("/exportTemplate")
    //@RequestPermissions("plm:product:detail:exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/productNoSpecDetailTemplate.xlsx";
        String excelName = "template.xlsx";

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
        }

    }

    /**
     * excel导出产品信息
     *
     * @param productSkuExcelDTO productSkuExcelDTO
     * @param response           response
     * @return com.erp.common.dto.base.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 11:49
     **/
    @PostMapping(value = "/exportProduct")
    //@RequestPermissions("plm:product:detail:exportProduct")
    public void exportProduct(@RequestBody ProductSkuExcelDTO productSkuExcelDTO, HttpServletResponse response) {
        productDetailService.exportProduct(productSkuExcelDTO, response);
    }


    @PostMapping(value = "/taskRefSku")
    public ApiResult<List<ProductDetailEntity>> getTaskRefSku(@RequestBody @Validated BaseIdDTO dto) {
        List<ProductDetailEntity> resultList = productDetailService.getSkuListByProductId(dto.getId());
        return success(resultList);
    }

    /**
     * @description: 设置审批人
     * @author Will
     * @date: 2022/11/28 16:43
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateApprover")
    public ApiResult updateApprover(@RequestBody @Validated ProductDetailApproveParamDTO dto) {
        Boolean result = productDetailService.updateApprover(dto);
        return result == true ? success() : failure();
    }

   /**
    * 产品信息-状态操作-审核通过
    * @author Will
    * @date: 2022/11/28 16:37
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/approvalPass")
    public ApiResult approvalPass(@RequestBody @Validated ProductDetailOperateDTO dto) {
        Boolean result = productDetailService.approvalPass(dto);
        return result == true ? success() : failure();
    }

   /**
    * 产品信息-状态操作-审核不通过
    * @author Will
    * @date: 2022/11/28 16:37
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/approvalReject")
    public ApiResult approvalNoPass(@RequestBody @Validated ProductDetailOperateDTO dto) {
        Boolean result = productDetailService.approvalReject(dto);
        return result == true ? success() : failure();
    }

}
