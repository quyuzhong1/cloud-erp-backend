package com.erp.server.plm.controller.api;

import com.alibaba.excel.EasyExcel;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProductWarehouseLocationExcelDTO;
import com.erp.model.plm.entity.ProductDetailApproverEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.listener.ProductWarehouseLocationListener;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 产品管理
 *
 * @Author Luo_WG
 * @Date 2022/9/22 11:48
 **/
@Slf4j
@RestController
@LogSystemModule("产品管理")
@RequestMapping("product/detail")
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

    @Resource
    private ProductUnitService productUnitService;

    @Resource
    private ProductDetailApproverService productDetailApproverService;

    @Resource
    private ProductCustomsService productCustomsService;

    /**
     * 临时接口-添加产品国外海关编码
     *
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/6/25 10:06
     **/
    @PostMapping("/addProductCustoms")
    public ApiResult addProductCustoms() {
        Boolean flag = productCustomsService.addProductCustoms();
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-主页列表-查询1
     *
     * @param pagingDTO pagingDTO
     * @return com.common.core.vo.ApiResult<com.erp.common.vo.PagingVO < com.erp.model.plm.dto.ProductDetailShowDTO>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:15
     **/
    @PostMapping("/list")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:detail:list", tableAlias = "pd")
    public ApiResult<PagingVO<ProductDetailShowDTO>> list(@RequestBody PagingDTO<ProductSkuDTO> pagingDTO) {
        PagingVO<ProductDetailShowDTO> paging = productDetailService.paging(pagingDTO);
        return this.success(paging);
    }

    /**
     * 根据sku进行模糊搜索
     * @param pagingDTO
     * @return
     */
    @PostMapping("/listSku" )
    public ApiResult<PagingVO<ProductDetailDTO.SkuDTO>> listSku(@RequestBody PagingDTO<ProductSkuDTO> pagingDTO){
        if (Objects.isNull(pagingDTO.getParams()) || StringUtils.isEmpty(pagingDTO.getParams().getRemoteSearchSku())){
            return success();
        }
        PagingVO<ProductDetailDTO.SkuDTO> skuDTOList = productDetailService.listSku(pagingDTO);
        return this.success(skuDTOList);
    }
    /**
     * 根据sku编号查询
     * @author Will
     * @date: 2023/10/24 12:06
     * @param skuParamDTO
     * @return ApiResult<List<ProductDetailShowDTO>>
     */
    @PostMapping("/listSkuBySkuNos")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:detail:list", tableAlias = "pd")
    public ApiResult<List<ProductSearchDTO.SkuListDTO>> listSkuBySkuNos(@RequestBody ProductSearchDTO.SkuParamDTO skuParamDTO) {
        List<ProductSearchDTO.SkuListDTO> list = productDetailService.listSkuBySkuNos(skuParamDTO);
        return this.success(list);
    }


    /**
     * 产品信息-无规格-产品详情-PLM-1.3
     *
     * @param productId 产品信息表id
     * @return com.common.core.vo.ApiResult<com.erp.model.plm.dto.ProductNoSpecDetailAllDTO>
     * @Author Luo_WG
     * @Date 2022/10/9 10:21
     **/
    @GetMapping("/getNoSpecDetailById")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:product:detail:getNoSpecDetailById",
            serviceClass = ProductInfoService.class,
            keyIdName = "productId"
    )
    public ApiResult<ProductNoSpecDetailAllDTO> getNoSpecDetailById(@RequestParam(value = "productId") String productId) {
        ProductNoSpecDetailAllDTO list = productDetailService.getNoSpecDetailById(productId);
        return this.success(list);
    }

    /**
     * 根据skuId查询产品信息-无规格-产品详情-PLM-1.3
     *
     * @param skuId 产品信息表id
     * @return com.common.core.vo.ApiResult<com.erp.model.plm.dto.ProductNoSpecDetailAllDTO>
     * @Author Luo_WG
     * @Date 2022/10/9 10:21
     **/
    @GetMapping("/getNoSpecDetailBySkuId")
    public ApiResult<ProductNoSpecDetailAllDTO> getNoSpecDetailBySkuId(@RequestParam(value = "skuId") String skuId) {
        ProductNoSpecDetailAllDTO list = productDetailService.getNoSpecDetailBySkuId(skuId);
        return this.success(list);
    }

    /**
     * 产品信息-多规格-产品详情-PLM-1.3
     *
     * @param productId 产品信息表id
     * @return com.common.core.vo.ApiResult<com.erp.model.plm.dto.ProductManyDetailDTO>
     * @Author Luo_WG
     * @Date 2022/10/9 10:22
     **/
    @GetMapping("/getManySpecDetailById")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:product:detail:getManySpecDetailById",
            serviceClass = ProductInfoService.class,
            keyIdName = "productId"
    )
    public ApiResult<ProductManyDetailDTO> getManySpecDetailById(@RequestParam(value = "productId") String productId) {
        ProductManyDetailDTO list = productDetailService.getManySpecDetailById(productId);
        return this.success(list);
    }


    /**
     * 产品信息-多规格-产品详情-编辑-PLM-1.3
     *
     * @param dto 产品信息表id
     * @return com.common.core.vo.ApiResult<com.erp.model.plm.dto.ProductManyDetailDTO>
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
     * 产品信息-无规格-产品详情-编辑-PLM-1.3
     *
     * @param dto 产品信息表id
     * @return com.common.core.vo.ApiResult<com.erp.model.plm.dto.ProductNoSpecDetailAllDTO>
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
        ProductNoSpecDetailAllDTO list = productDetailService.getNoSpecDetailBySkuId(dto.getId());
        return this.success(list);
    }

    /**
     * 产品信息-无规格-新增/修改-PLM-1.3
     *
     * @param productNoSpecDTO 新增产品无规格sku信息请求参数
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:22
     **/
    @LogAction(value = LogActionEnum.UNKNOWN_UPDATE, desc = "产品信息-无规格-新增/修改")
    @PostMapping("/saveOrUpdateNoSpec")
    //@RequestPermissions("plm:product:detail:saveOrUpdateNoSpec")
    public ApiResult saveOrUpdateNoSpec(@RequestBody @Validated ProductNoSpecDTO productNoSpecDTO) {
        Boolean flag = productDetailService.saveOrUpdateNoSpec(productNoSpecDTO);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-多规格-新增/修改-PLM-1.3
     *
     * @param productManySpecDTO 新增产品多规格sku信息请求参数
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:23
     **/
    @LogAction(value = LogActionEnum.UNKNOWN_UPDATE, desc = "产品信息-多规格-新增/修改")
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
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:23
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品信息-无规格-基础信息上传图片:产品表id={productId}")
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
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.entity.ProductDetailEntity>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:23
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "产品信息-多规格-自动生成：任务id={taskId}")
    @PostMapping("/InsertManySpecSku")
    //@RequestPermissions("plm:product:detail:InsertManySpecAuto")
    public ApiResult<List<ProductDetailEntity>> InsertManySpecAuto(@RequestBody @Validated VariantAutoAddDTO variantAutoAddDTO) {
        List<ProductDetailEntity> list = productDetailService.insertManySpecAuto(variantAutoAddDTO);
        return this.success(list);
    }

    /**
     * 添加已有sku到现有spu
     * @param changeSkuToSpuDTO
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "产品信息-多规格-添加sku关联")
    @PostMapping("/changeSkuBySpu")
    public ApiResult<List<ProductDetailEntity>> changeSkuBySpu(@RequestBody @Validated ChangeSkuToSpuDTO changeSkuToSpuDTO) {
        List<ProductDetailEntity> list = productDetailService.changeSkuBySpu(changeSkuToSpuDTO);
        return this.success(list);
    }
    /**
     * 产品信息-多规格sku-删除
     *
     * @param skuId sku表id
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:42
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "产品信息-多规格sku-删除")
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
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:42
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "产品信息-取消按钮-删除")
    @PostMapping("/deleteByProductId")
    //@RequestPermissions("plm:product:detail:delete")
    //@DataPermission(operationType = "deleteProduct", tableField = "create_user_id", menuCode = "plm:product:detail:delete", serviceClass = ProductDetailServiceImpl.class)
    public ApiResult deleteByProductId(@RequestParam(value = "id") String id) {
        Boolean flag = productDetailService.deleteByProductId(id);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-多规格sku-批量删除
     * @Author Luo_WG
     * @Date 2022/10/9 10:42
     * @param skuIds sku表id
     * @return com.common.core.vo.ApiResult
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
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.dto.ProductCostShowDTO>>
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
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.dto.ProductPurchaseShowDTO>>
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
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.dto.ProductSaleShowDTO>>
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
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.dto.ProductLogisticsShowDTO>>
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
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.dto.ProductPackShowDTO>>
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
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.dto.ProductCertificateShowDTO>>
     * @Author Luo_WG
     * @Date 2022/10/9 10:25
     **/
    @GetMapping("/listCertificate")
    public ApiResult<List<ProductCertificateShowDTO>> listCertificate(@RequestParam(value = "productId") String productId) {
        List<ProductCertificateShowDTO> list = productCertificateService.list(productId);
        return this.success(list);
    }


    /**
     * 采购信息-主页备注信息列表-查询
     *
     * @param productId:产品信息表id
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.entity.ProductPurchaseRemarkEntity>>
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
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "采购信息-备注信息-新增")
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
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     **/
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "采购信息-备注信息-新增-批量：id={id}")
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
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.entity.ProductVariantEntity>>
     **//*
    @GetMapping("/listVariant")
    public ApiResult<List<ProductVariantEntity>> listVariant() {
        List<ProductVariantEntity> list = productVariantService.list();
        return this.success(list);
    }*/

    /**
     * 产品信息-变体管理-下拉列表-查询
     *
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.entity.ProductVariantEntity>>
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
     * 产品信息-变体管理-下拉列表-新增/修改-PLM-1.3
     *
     * @param productVariantDTO 产品变体类型属性表
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:26
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品信息-变体管理-下拉列表-新增/修改:id={id},变体属性类型={propertyType}")
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
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "产品信息-变体管理-下拉列表-删除")
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
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.entity.ProductVariantPropertyEntity>>
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
     * @return com.common.core.vo.ApiResult
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
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:27
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "产品信息-变体管理-变体值-删除")
    @GetMapping("/deleteVariantProperty")
    public ApiResult deleteVariantProperty(@RequestParam(value = "variantPropertyId") String variantPropertyId) {
        Boolean flag = productVariantPropertyService.deleteVariant(variantPropertyId);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-单位管理-新增|修改
     *
     * @param productUnitList productUnitList
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:28
     **/
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "产品信息-单位管理-新增|修改：单位名称={name}")
    @PostMapping("/saveOrUpdateProductUnit")
    //@RequestPermissions("plm:product:detail:saveOrUpdateProductUnit")
    public ApiResult saveOrUpdateProductUnit(@RequestBody @Validated List<ProductUnitDTO> productUnitList) {
        Boolean flag = productUnitService.saveOrUpdateBatch(productUnitList);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品信息-单位管理-查询
     *
     * @return com.common.core.vo.ApiResult<java.util.List < com.erp.model.plm.entity.ProductUnitEntity>>
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
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 10:28
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "产品信息-单位管理-删除")
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
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入产品信息")
    @PostMapping("/importProductFile")
    //@RequestPermissions("plm:product:detail:importProductFile")
    public ApiResult importProductFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "importType") Integer importType, HttpServletResponse response) {
        Boolean flag = productDetailService.importProductFile(excelFile, importType, response);
        return flag == true ? success() : failure();
    }

    /**
     * 下载导出模板
     *
     * @param request  request
     * @param response response
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载导出模板")
    @GetMapping("/exportTemplate")
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * excel导出产品信息
     *
     * @param productSkuExcelDTO productSkuExcelDTO
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 11:49
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出产品信息")
    @PostMapping(value = "/exportProduct")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:detail:list", tableAlias = "pd")
    public ApiResult<Boolean> exportProduct(@RequestBody ProductSkuExcelDTO productSkuExcelDTO, HttpServletResponse response) {
        productDetailService.exportProduct(productSkuExcelDTO, response);
        return success(true);
    }


    @PostMapping(value = "/taskRefSku")
    public ApiResult<List<ProductDetailEntity>> getTaskRefSku(@RequestBody @Validated BaseIdDTO dto) {
        List<ProductDetailEntity> resultList = productDetailService.getSkuListByProductId(dto.getId());
        return success(resultList);
    }

    /**
     * 产品信息-设置审批人
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2022/11/28 16:43
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品信息-设置审批人:产品信息审批表id={id},审批人1={firstApproveId},审批人2={secondApproveId},审批人3={thirdApproveId}")
    @PostMapping("/updateApprover")
    public ApiResult updateApprover(@RequestBody @Validated ProductDetailApproveParamDTO dto) {
        Boolean result = productDetailService.updateApprover(dto);
        return result == true ? success() : failure();
    }

    /**
     * 产品信息-设置审批人回显
     *
     * @return ApiResult
     * @author Will
     * @date: 2022/11/28 16:43
     */
    @LogAction(value = LogActionEnum.UPDATE_WITHOUT_PARAMS, desc = "产品信息-设置审批人回显")
    @GetMapping("/getProductDetailApprover")
    public ApiResult<ProductDetailApproverEntity> getProductDetailApprover() {
        ProductDetailApproverEntity entity = productDetailApproverService.getProductDetailApprover();
        return success(entity);
    }

    /**
     * 产品信息-状态操作-审核通过
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2022/11/28 16:37
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "产品信息-状态操作-审核通过")
    @PostMapping("/approvalPass")
    public ApiResult approvalPass(@RequestBody @Validated ProductDetailOperateDTO dto) {
        Boolean result = productDetailService.approvalPass(dto,Boolean.TRUE);
        return result == true ? success() : failure();
    }

    /**
     * 产品信息-状态操作-审核不通过
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2022/11/28 16:37
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "产品信息-状态操作-审核不通过")
    @PostMapping("/approvalReject")
    public ApiResult approvalNoPass(@RequestBody @Validated ProductDetailOperateDTO dto) {
        Boolean result = productDetailService.approvalReject(dto);
        return result == true ? success() : failure();
    }

    /**
     * 产品信息-反审核
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2022/12/1 16:56
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "产品信息-反审核")
    @PostMapping("/deApprove")
    public ApiResult deApprove(@RequestBody @Validated ProductDetailOperateDTO dto) {
        Boolean result = productDetailService.deApprove(dto.getId());
        return result == true ? success() : failure();
    }


    /**
     * 产品信息-申请变更
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2022/12/1 15:41
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品信息-申请变更:产品信息id={id}")
    @PostMapping("/applyChange")
    public ApiResult applyChange(@RequestBody @Validated ProductDetailOperateDTO dto) {
        Boolean result = productDetailService.applyChange(dto.getId());
        return result == true ? success() : failure();
    }


    /**
     * 产品信息-重启审核流程
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2022/12/1 15:55
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品信息-重启审核流程:产品信息id={id}")
    @PostMapping("/restartProcessPass")
    public ApiResult restartProcessPass(@RequestBody @Validated ProductDetailOperateDTO dto) {
        Boolean result = productDetailService.restartProcessPass(dto);
        return result == true ? success() : failure();
    }

    /**
     * 产品信息-审核完成监听调用
     *
     * @param processId
     * @return ApiResult
     * @author Will
     * @date: 2022/12/1 15:21
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品信息-审核完成监听调用:流程id={processId}")
    @PostMapping("/productDetailProcessPass")
    public ApiResult productDetailProcessPass(String processId) {
        Boolean result = productDetailService.productDetailProcessPass(processId);
        return result == true ? success() : failure();
    }


    /**
     * 搜索sku
     *
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2023-01-11 14:58
     */
    @GetMapping("/search/sku")
    public ApiResult<List<SkuVO>> searchSku(String searchKeyword) {
        List<SkuVO> skuList = productDetailService.searchSku(searchKeyword);
        return success(skuList);
    }
    /**
     * 获取SKU下拉框-分页查询
     *
     * @return ApiResult<List < ShopInfoEntity>>
     * @author hyj
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<SkuVO>> pagingSelect(@RequestBody @Validated PagingDTO<SkuVO.SelectDTO> dto) {
        PagingVO<SkuVO> list = productDetailService.pagingSelect(dto);
        return success(list);
    }
    /**
     * 远程搜索包装辅料SKU
     * @author Will
     * @date: 2024/4/18 14:17
     * @param searchKeyword
     * @return ApiResult<List<SkuVO>>
     */
    @GetMapping("/search/accessoriesSku")
    public ApiResult<List<SkuVO>> accessoriesSku(String searchKeyword) {
        List<SkuVO> skuList = productDetailService.accessoriesSku(searchKeyword);
        return success(skuList);
    }

    /**
     * 搜索sku
     *
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2023-01-11 14:58
     */
    @GetMapping("/search/skuWithCombination")
    public ApiResult<List<SkuSimpleVO>> skuWithCombination(String searchKeyword) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<SkuSimpleVO> skuList = productDetailService.searchSkuWithCombination(searchKeyword);
        stopWatch.stop();
        log.warn(stopWatch.prettyPrint());
        return success(skuList);
    }


    /**
     * 搜索sku
     *
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2023-01-11 14:58
     */
    @PostMapping("/search/skuInfo")
    public ApiResult<List<SkuVO>> skuInfo(@RequestBody ProductDetailDTO.SearchDTO dto) {
        List<SkuVO> skuList = productDetailService.searchSkuInfo(dto);
        return success(skuList);
    }

    /**
     * 搜索父级sku
     *
     * @param searchKeyword
     * @return ApiResult<List < SkuVO>>
     * @author Will
     * @date: 2023/3/7 20:06
     */
    @GetMapping("/search/parentSku")
    public ApiResult<List<SkuVO>> searchParentSku(@Param("searchKeyword") String searchKeyword, @Param("bomId") String bomId) {
        List<SkuVO> skuList = productDetailService.searchParentSku(searchKeyword, bomId);
        return success(skuList);
    }


    /**
     * 在bom 管理 或者变更管理  获取到sku 信息
     * 根据sku id
     *
     * @param
     * @return com.common.core.vo.ApiResult<com.erp.model.plm.dto.ProductSmallestUnitDTO>
     * @author yl
     * @date 2023-01-29 14:03
     */
    @GetMapping("/skuInfo")
    public ApiResult<ProductSmallestUnitDTO> getSkuInfo(String skuId) {
        ProductSmallestUnitDTO sku = productDetailService.getSkuBySkuId(skuId);
        return success(sku);
    }

    /**
     * 产品信息-提交
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/2/9 13:34
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "产品信息-提交")
    @PostMapping("/commit")
    public ApiResult commit(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = productDetailService.commit(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 产品信息-反提交
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/2/9 13:34
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "产品信息-撤销")
    @PostMapping("/unCommit")
    public ApiResult unCommit(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = productDetailService.unCommit(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 产品信息-发送金蝶数据
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/2/13 13:30
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品信息-发送金蝶数据:id={id}")
    @PostMapping("/sendKingDeeData")
    public ApiResult sendKingDeeData(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = productDetailService.sendKingDeeData(dto.getId());
        return result == true ? success() : failure();
    }


    @GetMapping("/getSkuAuditStatus")
    public ApiResult getSkuAuditStatus() {
        List<Map<String, Object>> list = new ArrayList<>(10);
        for (ProductDetailStatusEnum state : ProductDetailStatusEnum.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", state.getName());
            map.put("status", state.getCode());
            list.add(map);
        }
        return success(list);

    }

    /**
     * @description: 更新负责人id
     * @author Will
     * @date: 2023/3/2 19:17
     */
    @LogAction(value = LogActionEnum.UPDATE_WITHOUT_PARAMS, desc = "更新负责人")
    @PostMapping("/handleChargeId")
    public void handleChargeId() {
        productDetailService.handleChargeId();
    }

    /**
     * 提交-PLM-1.3
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "产品详情提交")
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = productDetailService.submit(dto.getIds(), Boolean.TRUE);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核-PLM-1.3
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "产品详情批量审核")
    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<ProductDetailEntity> entityList = productDetailService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            ProductDetailEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"产品记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(productDetailService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("产品sku审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核-PLM-1.3
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "产品详情批量反审核")
    @PostMapping("/disApprove")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<ProductDetailEntity> entityList = productDetailService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            ProductDetailEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"产品记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(productDetailService.disApprove(entity));
            }catch (Exception e){
                log.error("产品sku审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程-PLM-1.3
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     **/
    @LogAction(value = LogActionEnum.CANCEL, desc = "产品详情取消流程")
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = productDetailService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除-PLM-1.3
     *
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "产品详情批量删除")
    @PostMapping("/deleteBatch")
    public ApiResult deleteBatch(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = productDetailService.deleteBatch(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量更新字段-PLM-1.3
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/6/15 11:32
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品详情批量更新字段:ids={ids},修改的字段名称编号={updateFiledCode}")
    @PostMapping("/updateBatchFiled")
    public ApiResult updateBatchFiled(@RequestBody @Validated ProductDetailBatchUpdateDTO dto) {
        Boolean flag = productDetailService.updateBatchFiled(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 根据sku id集合获取采购员、供应商信息
     *
     * @param idsDTO
     * @return
     */
    @PostMapping("/getPurchaseInfoBySkuIds")
    public ApiResult<List<SkuPurchaseDTO.PurchaseInfo>> getPurchaseInfoBySkuIds(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        return success(productPurchaseService.getInfoBySkuIds(idsDTO.getIds()));
    }

    /**
     * 根据产品id 获取到产品下的sku信息【PLM1.3】
     *
     * @param productId
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.plm.dto.SkuPurchaseDTO.PurchaseInfo>>
     * @author yl
     * @date 2023-06-25 10:17
     */
    @GetMapping("/listSkuByProductId")
    public ApiResult<List<ProductDetailEntity>> listSkuByProductId(@RequestParam("productId") String productId) {
        return success(productDetailService.getSkuListByProductId(productId));
    }

    /**
     * PDA:条件查询sku
     * @Author Luo_WG
     * @Date 2023/8/21 12:11
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.plm.vo.SkuVO>>
     **/
    @PostMapping("/search/pdaSearchSku")
    public ApiResult<List<SkuVO>> pdaSearchSku(@RequestBody ProductDetailDTO.PdaSearchDTO dto) {
        List<SkuVO> skuList = productDetailService.pdaSearchSku(dto);
        return success(skuList);
    }



    /**
     * excel导入产品仓位
     *
     * @param excelFile  文件流
     * @param response   响应
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入产品仓位")
    @PostMapping("/importProductWarehouseLocationFile")
    //@RequestPermissions("plm:product:detail:importProductFile")
    public ApiResult importProductWarehouseLocationFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        List<ProductDetailEntity> productDetailEntityList = productDetailService.list();
        ProductWarehouseLocationListener excelListenerUtil = new ProductWarehouseLocationListener(productDetailEntityList, productDetailService);
        try {
            EasyExcel.read(excelFile.getInputStream(), ProductWarehouseLocationExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95124);
        }
        List<ProductWarehouseLocationExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<ProductWarehouseLocationExcelDTO> list = excelListenerUtil.getDateList();
        if (list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/productWarehouseLocationError.xlsx";
            String name = "productWarehouseLocation";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }

            return failure();
        }
        return success();
    }

    /**
     * 下载导入模板
     *
     * @param request  request
     * @param response response
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载导入模板")
    @GetMapping("/importProductWarehouseLocationTemplate")
    public void importTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/productWarehouseLocationTemplate.xlsx";
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 首次推送sku到旺店通
     */
    @GetMapping("/initProductToWangDian")
    public ApiResult<String> initProductToWangDian(@RequestParam(required = false) List<String> ids){
        productDetailService.initProductToWangDian(ids);
        return success();
    }

    /**
     *初始化目的国海关信息
     *
     * @param skuIds  skuIds
     * @Author zdy
     * @Date 2024/5/08 11:46
     * @Desc 历史数据sku 增加默认值 并且把已存在目的国海关编码值移到custom中
     **/
    @PostMapping("/initProductCustom")
    public ApiResult initProductCustom(@RequestBody(required = false) List<String> skuIds) {
        productDetailService.initProductCustom(skuIds);
        return success();
    }

    /**
     * 打印EAN
     * @param printEanDTO 打印参数
     * @param response    响应
     */
    @PostMapping("/printEan")
    public void printEan(@RequestBody PrintEanDTO printEanDTO, HttpServletResponse response) {
        productDetailService.printEan(printEanDTO, response);
    }
}
