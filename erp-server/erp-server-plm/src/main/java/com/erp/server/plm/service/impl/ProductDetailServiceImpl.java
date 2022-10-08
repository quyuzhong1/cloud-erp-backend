package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.mapper.ProductInfoMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.ListUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 产品明细信息服务类
 * @Author: Luo_WG
 * @Date: 2022/9/21 16:25
 **/
@Service
public class ProductDetailServiceImpl extends ServiceImpl<ProductDetailMapper, ProductDetailEntity> implements ProductDetailService {

    @Resource
    private ProductDetailMapper productDetailMapper;

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

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductImagesService productImagesService;

    @Resource
    private ProductInfoMapper productInfoMapper;

    @Resource
    private ProductPurchaseRemarkService productPurchaseRemarkService;

    /**
     * @Description 产品信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param pagingDTO:查询参数
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     **/
    @Override
    public PagingVO<ProductDetailShowDTO> paging(PagingDTO<ProductSkuDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ProductDetailShowDTO> pageData = productDetailMapper.paging(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }

    /**
     * @Description 条件查询产品信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param name:产品名称
     * @return ProductDetailShowDTO
     **/
    @Override
    public ProductDetailShowDTO getProductBy(String name, String skuNo){
        return productDetailMapper.listProduct(name, skuNo);
    }

    /**
     * @Description 无规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:19
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductNoDetailDTO>
     **/
    @Override
    public ProductNoDetailDTO getNoSpecDetailById(String productId) {
        return productDetailMapper.getNoSpecDetailById(productId);
    }

    /**
     * @Description 多规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:19
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductManyDetailDTO>
     **/
    @Override
    public ProductManyDetailDTO getManySpecDetailById(String productId) {
        ProductManyDetailDTO productManyDetail = productDetailMapper.getManySpecDetailById(productId);
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getProductId, productId);
        List<ProductDetailEntity> list = this.list(queryWrapper);
        productManyDetail.setProductManySkuDetailList(list);
        return productManyDetail;
    }

    /**
     * @Description 保存/修改产品sku信息表数据-无规格
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productSkuBaseInfoDTO 新增产品无规格sku信息请求参数
     * @return java.lang.String
     **/
    @Override
    public String saveOrUpdate(ProductSkuBaseInfoDTO productSkuBaseInfoDTO) {
        ProductDetailEntity detailEntity = new ProductDetailEntity();
        BeanMapper.copy(productSkuBaseInfoDTO, detailEntity);
        this.saveOrUpdate(detailEntity);
        return detailEntity.getId();
    }

    /**
     * @Description 保存/修改产品sku信息表数据-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productDetailList 新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductDetailDTO> productDetailList) {
        List<ProductDetailEntity> list = BeanMapper.copyList(productDetailList, ProductDetailEntity.class);
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @Description 新增无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:55
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateNoSpec(ProductNoSpecDTO productNoSpecDTO) {
        //1.修改产品表 主表信息
        String id = productInfoService.updateSpec(productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO());

        //2.修改/新增 sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO();
        productSkuBaseInfoDTO.setProductId(id);
        //如果是修改sku图片 还需要修改图片表
        if(StringUtils.isNotBlank(productSkuBaseInfoDTO.getImagesUrl())){
            ProductImagesDTO productImagesDTO = new ProductImagesDTO();
            productImagesDTO.setSkuId(productSkuBaseInfoDTO.getId());
            productImagesDTO.setProductId(productSkuBaseInfoDTO.getProductId());
            productImagesDTO.setImagesUrl(productSkuBaseInfoDTO.getImagesUrl());
            productImagesService.updateProductImage(productImagesDTO);
        }
        String skuId = this.saveOrUpdate(productSkuBaseInfoDTO);
        if(StringUtils.isBlank(productSkuBaseInfoDTO.getId())){
            ProductDetailEntity productIdBySku = this.getProductIdBySku(productSkuBaseInfoDTO.getSkuNo());
            skuId = productIdBySku.getId();
        }

        //3.修改/新增 成本信息
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductCostDTO())) {
            ProductCostDTO productCostDTO = productNoSpecDTO.getProductCostDTO();
            productCostDTO.setSkuId(skuId);
            productCostService.saveOrUpdate(productNoSpecDTO.getProductCostDTO());
        }

        //4.修改/新增 采购信息
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductPurchaseDTO())) {
            ProductPurchaseDTO productPurchaseDTO = productNoSpecDTO.getProductPurchaseDTO();
            productPurchaseDTO.setSkuId(skuId);
            String purchaseId = productPurchaseService.saveOrUpdate(productNoSpecDTO.getProductPurchaseDTO());
            //新增/修改采购备注信息
            if (!ListUtils.isEmpty(productNoSpecDTO.getProductPurchaseDTO().getProductPurchaseRemarkList())) {
                List<ProductPurchaseRemarkDTO> productPurchaseRemarkList = productNoSpecDTO.getProductPurchaseDTO().getProductPurchaseRemarkList();
                productPurchaseRemarkList.forEach(req -> {
                    req.setPurchaseId(purchaseId);
                });
                productPurchaseRemarkService.saveOrUpdateBatch(productPurchaseRemarkList);
            }
        }

        //5.修改/新增 销售信息
        ProductSaleDTO productSaleDTO = productNoSpecDTO.getProductSaleDTO();
        if (!ObjectUtils.isEmpty(productSaleDTO)) {
            productSaleDTO.setSkuId(skuId);
            productSaleService.saveOrUpdate(productSaleDTO);
        }
        //6.修改/新增 物流信息
        ProductLogisticsDTO productLogisticsDTO = productNoSpecDTO.getProductLogisticsDTO();
        if (!ObjectUtils.isEmpty(productLogisticsDTO)) {
            productLogisticsDTO.setSkuId(skuId);
            productLogisticsService.saveOrUpdate(productLogisticsDTO);
        }

        //7.修改/新增 包装信息
        ProductPackDTO productPackDTO = productNoSpecDTO.getProductPackDTO();
        if (!ObjectUtils.isEmpty(productPackDTO)) {
            productPackDTO.setSkuId(skuId);
            productPackService.saveOrUpdate(productPackDTO);
        }

        //8.修改/新增 证书信息
        ProductCertificateDTO productCertificateDTO = productNoSpecDTO.getProductCertificateDTO();
        if (!ObjectUtils.isEmpty(productCertificateDTO)) {
            productCertificateDTO.setSkuId(skuId);
            productCertificateService.saveOrUpdate(productCertificateDTO);
        }

        return true;
    }

    /**
     * @Description 新增多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:52
     * @param productManySpecDTO:新增产品多规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateManySpec(ProductManySpecDTO productManySpecDTO) {
        //检查spu编号是否重复
        if (this.checkSpuNo(productManySpecDTO.getProductInfoDTO().getSpuNo())) {
            throw new ServiceException(ApiError.ERROR_95017);
        }

        //如果是修改允许保留原来的产品名称不变
        ProductDetailShowDTO productDetailShowDTO = this.getProductBy(productManySpecDTO.getProductInfoDTO().getName(), "");
        if (StringUtils.isNotBlank(productManySpecDTO.getProductInfoDTO().getId())) {
            if (!productDetailShowDTO.getId().equals(productManySpecDTO.getProductInfoDTO().getId())) {
                throw new ServiceException(ApiError.ERROR_95007);
            }
        } else {//如果是新增直接判断产品名称是否存在
            if (!ObjectUtils.isEmpty(productDetailShowDTO)) {
                throw new ServiceException(ApiError.ERROR_95007);
            }
        }

        //1.修改产品表 主表信息
        productInfoService.updateSpec(productManySpecDTO.getProductInfoDTO());
        //检查sku是否重复
        List<String> skuList = productManySpecDTO.getProductDetailList().stream().map(ProductDetailDTO::getSkuNo).collect(Collectors.toList());
        if (this.checkSkuNos(skuList)) {
            throw new ServiceException(ApiError.ERROR_95015);
        }
        //2.修改/新增 sku信息
        this.saveOrUpdateBatch(productManySpecDTO.getProductDetailList());
        //3.修改/新增 成本信息
        productCostService.saveOrUpdateBatch(productManySpecDTO.getProductCostList());
        //4.修改/新增 采购信息
        productPurchaseService.saveOrUpdateBatch(productManySpecDTO.getProductPurchaseList());
        //5.修改/新增 销售信息
        productSaleService.saveOrUpdateBatch(productManySpecDTO.getProductSaleList());
        //6.修改/新增 物流信息
        productLogisticsService.saveOrUpdateBatch(productManySpecDTO.getProductLogisticsList());
        //7.修改/新增 包装信息
        productPackService.saveOrUpdateBatch(productManySpecDTO.getProductPackList());
        //8.修改/新增 证书信息
        productCertificateService.saveOrUpdateBatch(productManySpecDTO.getProductCertificateList());
        return true;
    }

    /**
     * @Description 多规格自动生成
     * @Author Luo_WG
     * @Date 2022/9/26 14:54
     * @param variantAutoAddDTO:自动生成请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public List<ProductDetailEntity> insertManySpecAuto(VariantAutoAddDTO variantAutoAddDTO){
        List<VarianRefPropertyDTO> varianRefPropertyList = variantAutoAddDTO.getVarianRefPropertyList();
        List<String> varianTempList = new ArrayList<>();
        Boolean flag = true;
        for (VarianRefPropertyDTO req : varianRefPropertyList) {
            List<String> varianList = req.getVarianList();
            if (flag) {
                varianTempList.addAll(varianList);
            } else {
                for (int i = 0; i < varianList.size(); i++) {
                    for (String varian : varianList) {
                        varianTempList.set(i, varianTempList.get(i) + "," + varian);
                    }
                }
            }
            flag = false;
        }

        //把变体属性放入实体类
        List<ProductDetailEntity> list = new ArrayList<>();
        for (String req : varianTempList) {
            ProductDetailEntity productDetailEntity = new ProductDetailEntity();
            productDetailEntity.setVariantProperty(req);
            productDetailEntity.setProductId(variantAutoAddDTO.getProductId());
            productDetailEntity.setName(variantAutoAddDTO.getProductName());
            list.add(productDetailEntity);
        }

        //过滤掉重复的变体属性
        List<ProductDetailEntity> detailEntityList = this.queryByProductId(variantAutoAddDTO.getProductId());
        for (ProductDetailEntity req : detailEntityList) {
            if (list.contains(req.getVariantProperty())) {
                list.remove(req.getVariantProperty());
            }
        }
        boolean bool = this.saveBatch(list);
        if(!bool){
            throw new ServiceException(1, "新增sku明细失败！");
        }
        return this.queryByProductId(variantAutoAddDTO.getProductId());
    }

    public static void main(String[] args) {
        Set<String> set = new HashSet<>();
        set.add("a");
        set.add("a");
        set.add("b");
        System.out.println(set);
        set.forEach(req -> {
            System.out.println(req);
        });
    }

    /**
     * @Description 删除多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     * @param skuId:产品sku表主键id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean delete(String skuId){
        //1.删除证书信息
        productCertificateService.remove(skuId);
        //2.删除包装信息
        productPackService.remove(skuId);
        //3.删除物流信息
        productLogisticsService.remove(skuId);
        //4.删除销售信息
        productSaleService.remove(skuId);
        //5.删除采购信息
        productPurchaseService.remove(skuId);
        //6.删除成本信息
        productCostService.remove(skuId);
        //7.删除sku信息
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity:: getId, skuId);
        return this.remove(queryWrapper);
    }

    /**
     * @Description 根据产品主键id查询sku明细
     * @Author Luo_WG
     * @Date 2022/9/26 18:25
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     **/
    @Override
    public List<ProductDetailEntity> queryByProductId(String productId){
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductDetailEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    @Override
    public List<ProductDetailEntity> importProductFile(MultipartFile file, HttpServletRequest request) {


        return null;
    }

    /**
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     * @param skuList:sku集合
     **/
    public Boolean checkSkuNos(List<String> skuList) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductDetailEntity::getSkuNo, skuList);
        int count = this.count(queryWrapper);
        if(count > 0){
            return true;
        }
        return false;
    }
    /**
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     * @param sku:sku
     **/
    public Boolean checkSkuNo(String sku) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getSkuNo, sku);
        int count = this.count(queryWrapper);
        if(count > 0){
            return true;
        }
        return false;
    }

    /**
     * @Description 检查spu编号是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     * @param spuNo:spu编号
     * @return void
     **/
    public Boolean checkSpuNo(String spuNo) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductInfoEntity::getSpuNo, spuNo);
        Integer count = productInfoMapper.selectCount(queryWrapper);
        if(count > 0){
            return true;
        }
        return false;
    }

    /**
     * @Description 根据sku查询sku表信息
     * @Author Luo_WG
     * @Date 2022/9/28 17:04
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    public ProductDetailEntity getProductIdBySku(String sku) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getSkuNo, sku);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * @Description 新增无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:55
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean inportExcel(ProductNoSpecDTO productNoSpecDTO) {
        //根据产品名称查询产品信息
        ProductDetailShowDTO productByName = getProductBy(productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO().getName(), "");
        if (!ObjectUtils.isEmpty(productByName)) {
            if (!productByName.getSkuNo().equals(productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getSkuNo())) {
                throw new ServiceException(ApiError.ERROR_95007);
            }
        }

        //1.新增产品表 主表信息
        String id = productInfoService.updateSpec(productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO());

        //2.修改/新增 sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO();
        productSkuBaseInfoDTO.setProductId(id);
        //如果是修改sku图片 还需要修改图片表
        if(StringUtils.isNotBlank(productSkuBaseInfoDTO.getImagesUrl())){
            ProductImagesDTO productImagesDTO = new ProductImagesDTO();
            productImagesDTO.setSkuId(productSkuBaseInfoDTO.getId());
            productImagesDTO.setProductId(productSkuBaseInfoDTO.getProductId());
            productImagesDTO.setImagesUrl(productSkuBaseInfoDTO.getImagesUrl());
            productImagesService.updateProductImage(productImagesDTO);
        }
        String skuId = this.saveOrUpdate(productSkuBaseInfoDTO);
        if(StringUtils.isBlank(productSkuBaseInfoDTO.getId())){
            ProductDetailEntity productIdBySku = this.getProductIdBySku(productSkuBaseInfoDTO.getSkuNo());
            skuId = productIdBySku.getId();
        }

        ProductKeyDTO productKey = productDetailMapper.getProductKey(skuId);

        //3.修改/新增 成本信息
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductCostDTO())) {
            ProductCostDTO productCostDTO = productNoSpecDTO.getProductCostDTO();
            if (ObjectUtils.isNotEmpty(productKey)) {
                productCostDTO.setId(productKey.getCostId());
            }
            productCostDTO.setSkuId(skuId);
            productCostService.saveOrUpdate(productNoSpecDTO.getProductCostDTO());
        }

        //4.修改/新增 采购信息
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductPurchaseDTO())) {
            ProductPurchaseDTO productPurchaseDTO = productNoSpecDTO.getProductPurchaseDTO();
            if (ObjectUtils.isNotEmpty(productKey)) {
                productPurchaseDTO.setId(productKey.getPurchaseId());
            }
            productPurchaseDTO.setSkuId(skuId);
            String purchaseId = productPurchaseService.saveOrUpdate(productNoSpecDTO.getProductPurchaseDTO());
            //新增/修改采购备注信息
            if (!ListUtils.isEmpty(productNoSpecDTO.getProductPurchaseDTO().getProductPurchaseRemarkList())) {
                List<ProductPurchaseRemarkDTO> productPurchaseRemarkList = productNoSpecDTO.getProductPurchaseDTO().getProductPurchaseRemarkList();
                productPurchaseRemarkList.forEach(req -> {
                    req.setPurchaseId(purchaseId);
                });
                productPurchaseRemarkService.saveOrUpdateBatch(productPurchaseRemarkList);
            }
        }

        //5.修改/新增 销售信息
        ProductSaleDTO productSaleDTO = productNoSpecDTO.getProductSaleDTO();
        if (!ObjectUtils.isEmpty(productSaleDTO)) {
            if (ObjectUtils.isNotEmpty(productKey)) {
                productSaleDTO.setId(productKey.getSaleId());
            }
            productSaleDTO.setSkuId(skuId);
            productSaleService.saveOrUpdate(productSaleDTO);
        }
        //6.修改/新增 物流信息
        ProductLogisticsDTO productLogisticsDTO = productNoSpecDTO.getProductLogisticsDTO();
        if (!ObjectUtils.isEmpty(productLogisticsDTO)) {
            if (ObjectUtils.isNotEmpty(productKey)) {
                productLogisticsDTO.setId(productKey.getLogisticsId());
            }
            productLogisticsDTO.setSkuId(skuId);
            productLogisticsService.saveOrUpdate(productLogisticsDTO);
        }

        //7.修改/新增 包装信息
        ProductPackDTO productPackDTO = productNoSpecDTO.getProductPackDTO();
        if (!ObjectUtils.isEmpty(productPackDTO)) {
            if (ObjectUtils.isNotEmpty(productKey)) {
                productPackDTO.setId(productKey.getPackId());
            }
            productPackDTO.setSkuId(skuId);
            productPackService.saveOrUpdate(productPackDTO);
        }

        //8.修改/新增 证书信息
        /*ProductCertificateDTO productCertificateDTO = productNoSpecDTO.getProductCertificateDTO();
        if (!ObjectUtils.isEmpty(productCertificateDTO)) {
            productCertificateDTO.setSkuId(skuId);
            productCertificateService.saveOrUpdate(productCertificateDTO);
        }*/
        return true;
    }
}
