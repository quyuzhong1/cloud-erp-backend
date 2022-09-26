package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

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

    /**
     * @Description 产品信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param sku:此处可能是spu，需求界面只有一个输入框可输入spu或者sku查询
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
     * @Description 新增无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:55
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateNoSpec(ProductNoSpecDTO productNoSpecDTO) {

        //1.修改产品表 主表信息

        //2.修改/新增 成本信息
        productCostService.saveOrUpdate(productNoSpecDTO.getProductCostDTO());
        //3.修改/新增 采购信息
        productPurchaseService.saveOrUpdate(productNoSpecDTO.getProductPurchaseDTO());
        //4.修改/新增 销售信息
        productSaleService.saveOrUpdate(productNoSpecDTO.getProductSaleDTO());
        //5.修改/新增 物流信息
        productLogisticsService.saveOrUpdate(productNoSpecDTO.getProductLogisticsDTO());
        //6.修改/新增 包装信息
        productPackService.saveOrUpdate(productNoSpecDTO.getProductPackDTO());
        //7.修改/新增 证书信息
        productCertificateService.saveOrUpdate(productNoSpecDTO.getProductCertificateDTO());

        //如果没有id表示新增
        ProductDetailEntity productDetail = new ProductDetailEntity();
        BeanUtils.copyProperties(productNoSpecDTO, ProductDetailEntity.class);
        return null;
    }

    /**
     * @Description 新增多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:52
     * @param productManySpecDTO:新增产品多规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean insertProductManySpec(ProductManySpecDTO productManySpecDTO) {
        //TODO 1.校验产品名称是否存在
        //TODO 2.需要修改产品主表信息
        //TODO 3.需要修改或者新增产品sku信息
        //TODO 4.校验属性重复项：同个SPU，属性值不能重复
        //TODO 5.校验SPU重复：已存在SPU:{SPU名称}
        //TODO 6.校验SKU重复：已存在SKU:{SKU名称}
        return null;
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
        //TODO 1.校验SKU状态，封样后不可删除
        //TODO 2.需要同时删除关联表的所以信息
        return null;
    }

    /**
     * @param sku
     * @return void
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/21 18:23
     **/
    private void checkSku(String sku) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getSku, sku);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(/*ApiError.ERROR_95012*/);
        }
    }

    /**
     * @param sku
     * @return void
     * @Description 检查产品名称是否重复
     * @Author Luo_WG
     * @Date 2022/9/21 18:23
     **/
    private void checkName(String sku) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getName, sku);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(/*ApiError.ERROR_95012*/);
        }
    }
}
