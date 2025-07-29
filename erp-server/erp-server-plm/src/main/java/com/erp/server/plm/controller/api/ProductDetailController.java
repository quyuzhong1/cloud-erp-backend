package com.erp.server.plm.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ExcelImportFsDTO;
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
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.listener.ProductWarehouseLocationListener;
import com.erp.server.plm.query.ProductDetailQueryHandler;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Resource
    private ProductDetailImagesService productDetailImagesService;

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
    @WebAdvanceQuery(handler = ProductDetailQueryHandler.class)
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
    public ApiResult<?> saveOrUpdatePurchaseRemarkBatch(@RequestBody List<ProductPurchaseRemarkDTO> dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.size());
        List<String> ids = dto.stream().map(ProductPurchaseRemarkDTO::getId).collect(Collectors.toList());
        Map<String, ProductPurchaseRemarkEntity> entityMap = productPurchaseRemarkService.listByIds(ids)
                .stream()
                .collect(Collectors.toMap(ProductPurchaseRemarkEntity::getId, Function.identity()));
        for (ProductPurchaseRemarkDTO remarkDTO : dto) {
            ProductPurchaseRemarkEntity entity = entityMap.get(remarkDTO.getId());
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(remarkDTO.getId(), remarkDTO.getId(), "采购信息不存在"));
                continue;
            }
            try {
                Boolean result = productPurchaseRemarkService.saveOrUpdateBatch(Collections.singletonList(remarkDTO));
                if (result){
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getProductId(), "采购信息-备注信息-新增成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getProductId(), "采购信息-备注信息-新增失败"));
                }
            }catch (Exception e){
                log.error("采购信息-备注信息添加失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getProductId(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品信息-单位管理-新增|修改：单位名称={name}", keyIdName = "name")
    @PostMapping("/saveOrUpdateProductUnit")
    //@RequestPermissions("plm:product:detail:saveOrUpdateProductUnit")
    public ApiResult<?> saveOrUpdateProductUnit(@RequestBody @Validated List<ProductUnitDTO> productUnitList) {
        List<BatchResultDTO> resultDTOS = new LinkedList<>();
        for (ProductUnitDTO dto : productUnitList) {
            try {
                Boolean flag = productUnitService.saveOrUpdateBatch(Collections.singletonList(dto));
                if (flag){
                    resultDTOS.add(BatchResultDTO.success(dto.getId(), dto.getName(),"单位管理-新增|修改成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(dto.getId(),dto.getName(),"单位管理-新增|修改失败"));
                }
            }catch (Exception e){
                log.error("单位管理-新增|修改失败",e);
                resultDTOS.add(BatchResultDTO.fail(dto.getId(), dto.getName(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
    public ApiResult<ExcelImportFsDTO.UrlDTO> importProductFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "importType") Integer importType, HttpServletResponse response) {
        ExcelImportFsDTO.UrlDTO urlDTO = productDetailService.importProductFile(excelFile, importType, response);
        return success(urlDTO);
    }

    /**
     * excel更新导入
     **/
    @LogAction(value = LogActionEnum.IMPORT, desc = "更新产品信息")
    @PostMapping("/importProductUpdate")
    public ApiResult<ExcelImportFsDTO.UrlDTO> importProductUpdate(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        ExcelImportFsDTO.UrlDTO urlDTO = productDetailService.importProductUpdate(excelFile, response);
        return success(urlDTO);
    }

    /**
     * 下载导出更新模板
     *
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载导出模板")
    @GetMapping("/exportUpdateTemplate")
    public void exportUpdateTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "excel/productUpdateTemplate.xlsx";
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
     * 下载导出模板
     *
     * @param request  request
     * @param response response
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载导出模板")
    @GetMapping("/exportTemplate")
    public void exportTemplate(@RequestParam(value = "importType") Integer importType,HttpServletRequest request, HttpServletResponse response) {
        String path = "";
        String excelName = "template.xlsx";
        if(importType == 1){//导入新增
            path = "classpath:excel/productNoSpecDetailTemplate.xlsx";
        }else if(importType == 2){//导入更新（待审核）
            path = "classpath:excel/productUpdateNotApproveTemplate.xlsx";
        }else if(importType == 3){//导入更新（已审核）
            path = "classpath:excel/productUpdateApproveTemplate.xlsx";
        }
        if(StringUtils.isEmpty(path)){
            throw new ServiceException(ApiError.ERROR_99999);
        }

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
    @WebAdvanceQuery(handler = ProductDetailQueryHandler.class)
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
    public ApiResult<List<SkuSimpleVO>> skuWithCombination(@RequestParam(value = "searchKeyword",required = false) String searchKeyword) {
        return success(productDetailService.searchSkuWithCombination(searchKeyword));
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
     * 搜索sku
     *
     * @return com.common.core.vo.ApiResult
     * @author jack
     * @date 2025-07-11
     */
    @PostMapping("/search/checkParams/skuInfo")
    public ApiResult<List<SkuVO>> searchCheckParamsSkuInfo(@RequestBody ProductDetailDTO.SearchDTO dto) {
        if (Objects.isNull(dto) || StringUtils.isEmpty(dto.getSearchKeyword())){
            return success();
        }
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
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = productDetailService.submit(id,Boolean.TRUE);
            }catch (Exception e){
                log.error("产品信息单 提交审核失败",e);
                ProductDetailEntity entity = productDetailService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "产品信息单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = productDetailService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()),Boolean.TRUE);
            }catch (Exception e){
                log.error("产品信息审核失败",e);
                ProductDetailEntity entity = productDetailService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "产品信息单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(approveResult);
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
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = productDetailService.cancelProcess(id);
            }catch (Exception e){
                log.error("产品信息撤回流程失败",e);
                ProductDetailEntity entity = productDetailService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "产品信息不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
    public ApiResult<?> updateBatchFiled(@RequestBody @Validated ProductDetailBatchUpdateDTO dto) {
        List<BatchResultDTO> resultDTOS = new LinkedList<>();
        Map<String, ProductDetailEntity> entityMap = productDetailService.listByIds(dto.getIds())
                .stream()
                .collect(Collectors.toMap(ProductDetailEntity::getId, e -> e));
        for (String id : dto.getIds()) {
            ProductDetailEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"产品明细不存在"));
                continue;
            }
            try {
                Boolean flag = productDetailService.updateBatchFiled(new ProductDetailBatchUpdateDTO(
                        Collections.singletonList(id),
                        dto.getUpdateFiledCode(),
                        dto.getValues(),
                        dto.getName()
                ));
                if (flag){
                    resultDTOS.add(BatchResultDTO.success(id,entity.getName(),"产品详情批量更新成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id,entity.getName(),"产品详情批量更新失败"));
                }
            }catch (Exception e){
                log.error("产品详情批量更新失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
     * 首次推送sku到领星
     */
    @GetMapping("/initProductToLingXing")
    public ApiResult<String> initProductToLingXing(@RequestParam(required = false) List<String> ids){
        productDetailService.initProductToLingXing(ids);
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

    /**
     * 目的国申报价重算
     * @param dto
     * @return
     */
    @PostMapping("/resetDestDeclarePrice")
    public ApiResult<List<BatchResultDTO>> resetDestDeclarePrice(@RequestBody @Validated BaseIdsDTO.IdsDTO dto){
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listByIds(ids);
        List<BatchResultDTO> resultDTOS = productDetailService.resetDestDeclarePrice(productDetailEntityList, Boolean.TRUE);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 根据skuIds获取产品包装尺寸明细
     * @param dto 参数
     */
    @PostMapping("/listProductPackBySkuIds")
    public ApiResult<List<ProductPackViewDTO>> listProductPackBySkuIds(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<ProductPackViewDTO> productPackViewDTOS = productDetailService.listProductPackBySkuIds(dto.getIds());
        return success(productPackViewDTOS);
    }

    /**
     * 修改产品包装尺寸
     * @param dto 参数
     */
    @PostMapping("/batchUpdateProductPack")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "修改产品包装尺寸")
    public ApiResult<List<BatchResultDTO>> batchUpdateProductPack(@RequestBody @Validated BatchParamsDTO<ProductPackViewDTO> dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (ProductPackViewDTO viewDTO : dto.getParams()) {
            try {
                resultDTOS.add(productDetailService.updateProductPack(viewDTO));
            } catch (Exception e) {
                log.error("产品sku修改包装尺寸失败", e);
                resultDTOS.add(BatchResultDTO.fail(viewDTO.getSkuId(), viewDTO.getSkuNo(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     *  上传SKU图片（主页）
     * @Author jack
     * @Date 2025-07-25
     **/
    @PostMapping("/uploadProductImage")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:product:detail:uploadProductImage",
            serviceClass = ProductDetailImagesService.class,
            keyIdName = "id"
    )
    public ApiResult uploadProductImage(@RequestBody @Validated ProductDetailDTO.ProductImagesDTO dto) {
        Boolean flag = productDetailImagesService.uploadProductImage(dto);
        return flag == true ? this.success() : this.failure();
    }

    /**
     *  上传zip包 图片
     * @Author jack
     * @Date 2025-07-25
     **/
    @PostMapping(value = "/importZip")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:product:detail:uploadProductImage",
            serviceClass = ProductDetailImagesService.class,
            keyIdName = "id"
    )
    public ApiResult<Object> importZip(@RequestBody ProductDetailDTO.ProductImagesZipDTO dto) {
        Boolean flag = productDetailImagesService.importZip(dto);
        return flag == true ? success() : failure();
    }

}
