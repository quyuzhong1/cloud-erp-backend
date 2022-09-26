package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.model.plm.entity.ProductVariantEntity;
import com.erp.model.plm.entity.ProductVariantPropertyEntity;
import com.erp.server.plm.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 产品明细表 前端控制器
 * @Author Luo_WG
 * @Date 2022/9/22 11:48
 **/
@Api(tags = "产品管理")
@RestController
@RequestMapping("plm/product/detail")
public class ProductDetailController extends BaseController {

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

    @ApiOperation(value = "产品信息-主页列表-查询")
    @GetMapping("/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sku", value = "sku/spu"),
    })
    public ApiResult<List<ProductDetailShowDTO>> list(@RequestParam(value = "sku") String sku) {
        List<ProductDetailShowDTO> list = productDetailService.list(sku);
        return this.success(list);
    }

    @ApiOperation(value = "产品信息-无规格-产品详情")
    @GetMapping("/getNoSpecDetailById")
    @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true)
    public ApiResult<ProductNoDetailDTO> getNoSpecDetailById(@RequestParam(value = "productId") String productId) {
        ProductNoDetailDTO list = productDetailService.getNoSpecDetailById(productId);
        return this.success(list);
    }

    @ApiOperation(value = "产品信息-多规格-产品详情")
    @GetMapping("/getManySpecDetailById")
    @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true)
    public ApiResult<ProductManyDetailDTO> getManySpecDetailById(@RequestParam(value = "productId") String productId) {
        ProductManyDetailDTO list = productDetailService.getManySpecDetailById(productId);
        return this.success(list);
    }

    @ApiOperation(value = "产品信息-无规格-新增/修改")
    @PostMapping("/saveOrUpdateNoSpec")
    public ApiResult saveOrUpdateNoSpec(@RequestBody ProductNoSpecDTO productNoSpecDTO) {
        Boolean flag = productDetailService.saveOrUpdateNoSpec(productNoSpecDTO);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "产品信息-多规格-新增/修改")
    @PostMapping("/saveOrUpdateManySpec")
    public ApiResult saveOrUpdateManySpec(@RequestBody ProductManySpecDTO productManySpecDTO) {
        Boolean flag = productDetailService.saveOrUpdateManySpec(productManySpecDTO);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "产品信息-无规格-基础信息上传图片")
    @PostMapping("/insertProductImage")
    public ApiResult insertProductImage(@RequestBody ProductImagesDTO productImagesDTO) {
        Boolean flag = productImagesService.insertProductImage(productImagesDTO);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "产品信息-多规格-自动生成")
    @PostMapping("/InsertManySpecSku")
    public ApiResult<List<ProductDetailEntity>> InsertManySpecAuto(@RequestBody VariantAutoAddDTO variantAutoAddDTO) {
        List<ProductDetailEntity> list = productDetailService.InsertManySpecAuto(variantAutoAddDTO);
        return this.success(list);
    }

    @ApiOperation(value = "产品信息-多规格sku-删除")
    @PostMapping("/delete")
    @ApiImplicitParam(name = "skuId", value = "sku信息表id", required = true)
    public ApiResult delete(@RequestBody String skuId) {
        Boolean flag = productDetailService.delete(skuId);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "成本信息-主页列表-查询")
    @GetMapping("/listCost")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductCostShowDTO>> listCost(@RequestParam(value = "productId") String productId) {
        List<ProductCostShowDTO> list = productCostService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "采购信息-主页列表-查询")
    @GetMapping("/listProductPurchase")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductPurchaseShowDTO>> listPurchase(@RequestParam(value = "productId") String productId) {
        List<ProductPurchaseShowDTO> list = productPurchaseService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "销售信息-主页列表-查询")
    @GetMapping("/listSale")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductSaleShowDTO>> listSale(@RequestParam(value = "productId") String productId) {
        List<ProductSaleShowDTO> list = productSaleService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "物流信息-报关信息列表-查询")
    @GetMapping("/listLogistics")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductLogisticsShowDTO>> listLogistics(@RequestParam(value = "productId") String productId) {
        List<ProductLogisticsShowDTO> list = productLogisticsService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "物流信息-包装信息列表-查询")
    @GetMapping("/listPack")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductPackShowDTO>> listPack(@RequestParam(value = "productId") String productId) {
        List<ProductPackShowDTO> list = productPackService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "证书信息-主页列表-查询")
    @GetMapping("/listCertificate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductCertificateShowDTO>> listCertificate(@RequestParam(value = "productId") String productId) {
        List<ProductCertificateShowDTO> list = productCertificateService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "采购信息-主页备注信息列表-查询")
    @GetMapping("/listPurchaseRemark")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "purchaseId", value = "产品采购信息表id", required = true),
    })
    public ApiResult<List<ProductPurchaseRemarkEntity>> listPurchaseRemark(@RequestParam(value = "purchaseId") String purchaseId) {
        List<ProductPurchaseRemarkEntity> list = productPurchaseRemarkService.list(purchaseId);
        return this.success(list);
    }

    @ApiOperation(value = "采购信息-备注信息-新增")
    @PostMapping("/saveOrUpdatePurchaseRemark")
    public ApiResult saveOrUpdatePurchaseRemark(ProductPurchaseRemarkDTO dto) {
        Boolean flag = productPurchaseRemarkService.saveOrUpdate(dto);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "产品信息-变体管理-下拉列表-查询")
    @GetMapping("/listVariant")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productId", value = "产品信息表id", required = true),
    })
    public ApiResult<List<ProductVariantEntity>> listVariant(@RequestParam(value = "productId") String productId) {
        List<ProductVariantEntity> list = productVariantService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "产品信息-变体管理-下拉列表-新增/修改")
    @PostMapping("/saveOrUpdateVariant")
    public ApiResult saveOrUpdateVariant(ProductVariantDTO productVariantDTO) {
        Boolean flag = productVariantService.saveOrUpdate(productVariantDTO);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "产品信息-变体管理-下拉列表-删除")
    @PostMapping("/deleteVariant")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "variantId", value = "变体类型表主键Id", required = true),
    })
    public ApiResult deleteVariant(@RequestParam(value="variantId") String variantId){
        Boolean flag = productVariantService.deleteVariant(variantId);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "产品信息-变体管理-变体值-查询")
    @GetMapping("/listVariantProperty")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "variantId", value = "变体类型表id", required = true),
    })
    public ApiResult<List<ProductVariantPropertyEntity>> listVariantProperty(@RequestParam(value = "variantId") String variantId) {
        List<ProductVariantPropertyEntity> list = productVariantPropertyService.list(variantId);
        return this.success(list);
    }

    @ApiOperation(value = "产品信息-变体管理-变体值-新增/修改")
    @PostMapping("/saveOrUpdateVariantProperty")
    public ApiResult saveOrUpdateVariantProperty(ProductVariantPropertyDTO dto) {
        Boolean flag = productVariantPropertyService.saveOrUpdate(dto);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "产品信息-变体管理-变体值-删除")
    @PostMapping("/deleteVariantProperty")
    public ApiResult deleteVariantProperty(ProductVariantPropertyDTO dto) {
        Boolean flag = productVariantPropertyService.saveOrUpdate(dto);
        return flag == true ? this.success() : this.failure();
    }
}
