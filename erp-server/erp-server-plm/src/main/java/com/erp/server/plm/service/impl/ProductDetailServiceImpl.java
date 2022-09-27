package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.mapper.ProductInfoMapper;
import com.erp.server.plm.service.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * @Description 产品信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param sku:此处可能是spu，需求界面只有一个输入框可输入spuNo或者skuNo查询
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     **/
    @Override
    public List<ProductDetailShowDTO> list(String sku) {
        return productDetailMapper.list(sku);
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
        queryWrapper.eq(ProductDetailEntity::getProperty, productId);
        List<ProductDetailEntity> list = this.list(queryWrapper);
        productManyDetail.setProductManySkuDetailList(list);
        return productManyDetail;
    }

    /**
     * @Description 保存/修改产品sku信息表数据-无规格
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productNoSpecDTO 新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductNoSpecDTO productNoSpecDTO) {
        ProductDetailEntity detailEntity = new ProductDetailEntity();
        BeanMapper.copy(productNoSpecDTO, detailEntity);
        return this.saveOrUpdate(detailEntity);
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
        productInfoService.updateSpec(productNoSpecDTO.getProductInfoDTO());
        //如果是修改sku图片 还需要修改图片表
        if(!StringUtils.isBlank(productNoSpecDTO.getImagesUrl())){
            ProductImagesDTO productImagesDTO = new ProductImagesDTO();
            productImagesDTO.setSkuId(productNoSpecDTO.getId());
            productImagesDTO.setProductId(productNoSpecDTO.getProductId());
            productImagesDTO.setImagesUrl(productNoSpecDTO.getImagesUrl());
            productImagesService.updateProductImage(productImagesDTO);
        }
        //2.修改/新增 sku信息
        this.saveOrUpdate(productNoSpecDTO);
        //3.修改/新增 成本信息
        productCostService.saveOrUpdate(productNoSpecDTO.getProductCostDTO());
        //4.修改/新增 采购信息
        productPurchaseService.saveOrUpdate(productNoSpecDTO.getProductPurchaseDTO());
        //5.修改/新增 销售信息
        productSaleService.saveOrUpdate(productNoSpecDTO.getProductSaleDTO());
        //6.修改/新增 物流信息
        productLogisticsService.saveOrUpdate(productNoSpecDTO.getProductLogisticsDTO());
        //7.修改/新增 包装信息
        productPackService.saveOrUpdate(productNoSpecDTO.getProductPackDTO());
        //8.修改/新增 证书信息
        productCertificateService.saveOrUpdate(productNoSpecDTO.getProductCertificateDTO());
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
        //检查sku产品名称是否重复
        this.checkName(productManySpecDTO.getProductInfoDTO().getName());
        //1.修改产品表 主表信息
        productInfoService.updateSpec(productManySpecDTO.getProductInfoDTO());
        //检查sku是否重复
        List<String> skuList = productManySpecDTO.getProductDetailList().stream().map(ProductDetailDTO::getSku).collect(Collectors.toList());
        this.checkSku(skuList);
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
    public List<ProductDetailEntity> InsertManySpecAuto(VariantAutoAddDTO variantAutoAddDTO){
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
        List<ProductDetailEntity> list = new ArrayList<>();
        varianTempList.forEach(req -> {
            ProductDetailEntity productDetailEntity = new ProductDetailEntity();
            productDetailEntity.setProperty(req);
            list.add(productDetailEntity);
        });
        List<ProductDetailEntity> detailEntityList = this.queryByProductId(variantAutoAddDTO.getProductId());
        detailEntityList.forEach(req -> {
            if (list.contains(req.getProperty())) {
                list.remove(req.getProperty());
            }
        });
        boolean bool = this.saveBatch(list);
        if(!bool){
            throw new ServiceException(1, "新增sku明细失败！");
        }
        return this.queryByProductId(variantAutoAddDTO.getProductId());
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

    /**
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     * @param skuList:sku集合
     **/
    private void checkSku(List<String> skuList) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductDetailEntity::getSku, skuList);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95015);
        }
    }

    /**
     * @Description 检查sku产品名称是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     * @param name:产品名称集合
     * @return void
     **/
    private void checkName(String name) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductInfoEntity::getName, name);
        Integer count = productInfoMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95007);
        }
    }
}
