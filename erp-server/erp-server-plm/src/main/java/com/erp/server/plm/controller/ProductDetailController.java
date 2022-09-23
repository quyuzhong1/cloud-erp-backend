package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.*;
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
    private ProductSaleService productSaleService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private ProductPackService productPackService;

    @Resource
    private ProductCertificateService productCertificateService;

    @ApiOperation(value = "产品信息-主页列表")
    @GetMapping("/list")
    @ApiImplicitParams({
        @ApiImplicitParam(name="sku",value="sku/spu"),
    })
    public ApiResult<List<ProductDetailShowDTO>> list(@RequestParam(value = "sku") String sku){
        List<ProductDetailShowDTO> list = productDetailService.list(sku);
        return this.success(list);
    }

    @ApiOperation(value = "产品信息-无规格-产品详情")
    @GetMapping("/getNoSpecDetailById")
    @ApiImplicitParam(name="productId",value="产品信息表id")
    public ApiResult<ProductNoDetailDTO> getNoSpecDetailById(@RequestParam(value = "productId") String productId){
        ProductNoDetailDTO list = productDetailService.getNoSpecDetailById(productId);
        return this.success(list);
    }

    @ApiOperation(value = "产品信息-多规格-产品详情")
    @GetMapping("/getManySpecDetailById")
    @ApiImplicitParam(name="productId",value="产品信息表id")
    public ApiResult<ProductManyDetailDTO> getManySpecDetailById(@RequestParam(value = "productId") String productId){
        ProductManyDetailDTO list = productDetailService.getManySpecDetailById(productId);
        return this.success(list);
    }

    @ApiOperation(value = "产品信息-无规格-新增/修改")
    @PostMapping("/saveOrUpdateNoSpec")
    public ApiResult saveOrUpdateNoSpec(@RequestBody ProductNoSpecDTO productNoSpecDTO){
        Boolean flag = productDetailService.saveOrUpdateNoSpec(productNoSpecDTO);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "产品信息-多规格-新增")
    @PostMapping("/insertProductManySpec")
    public ApiResult insertProductManySpec(@RequestBody ProductManySpecDTO productManySpecDTO){
        Boolean flag = productDetailService.insertProductManySpec(productManySpecDTO);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "产品信息-多规格-删除sku")
    @PostMapping("/delete")
    @ApiImplicitParam(name="skuId",value="sku信息表id")
    public ApiResult delete(@RequestBody String skuId){
        Boolean flag = productDetailService.delete(skuId);
        return flag == true ? this.success() : this.failure();
    }

    @ApiOperation(value = "成本信息-主页列表")
    @GetMapping("/listCost")
    @ApiImplicitParams({
        @ApiImplicitParam(name="productId",value="产品信息表id"),
    })
    public ApiResult<List<ProductCostShowDTO>> listCost(@RequestParam(value = "productId") String productId){
        List<ProductCostShowDTO> list = productCostService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "采购信息-主页列表")
    @GetMapping("/listProductPurchase")
    @ApiImplicitParams({
            @ApiImplicitParam(name="productId",value="产品信息表id"),
    })
    public ApiResult<List<ProductPurchaseShowDTO>> listPurchase(@RequestParam(value = "productId") String productId){
        List<ProductPurchaseShowDTO> list = productPurchaseService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "销售信息-主页列表")
    @GetMapping("/listSale")
    @ApiImplicitParams({
            @ApiImplicitParam(name="productId",value="产品信息表id"),
    })
    public ApiResult<List<ProductSaleShowDTO>> listSale(@RequestParam(value = "productId") String productId){
        List<ProductSaleShowDTO> list = productSaleService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "物流信息-报关信息-列表")
    @GetMapping("/listLogistics")
    @ApiImplicitParams({
            @ApiImplicitParam(name="productId",value="产品信息表id"),
    })
    public ApiResult<List<ProductLogisticsShowDTO>> listLogistics(@RequestParam(value = "productId") String productId){
        List<ProductLogisticsShowDTO> list = productLogisticsService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "物流信息-包装信息-列表")
    @GetMapping("/listPack")
    @ApiImplicitParams({
            @ApiImplicitParam(name="productId",value="产品信息表id"),
    })
    public ApiResult<List<ProductPackShowDTO>> listPack(@RequestParam(value = "productId") String productId){
        List<ProductPackShowDTO> list = productPackService.list(productId);
        return this.success(list);
    }

    @ApiOperation(value = "证书信息-主页列表")
    @GetMapping("/listCertificate")
    @ApiImplicitParams({
            @ApiImplicitParam(name="productId",value="产品信息表id"),
    })
    public ApiResult<List<ProductCertificateShowDTO>> listCertificate(@RequestParam(value = "productId") String productId){
        List<ProductCertificateShowDTO> list = productCertificateService.list(productId);
        return this.success(list);
    }
}
