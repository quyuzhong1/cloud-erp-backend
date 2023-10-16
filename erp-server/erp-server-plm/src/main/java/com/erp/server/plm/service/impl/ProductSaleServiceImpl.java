package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.dto.NewProductDTO;
import com.erp.model.plm.dto.ProductSaleDTO;
import com.erp.model.plm.dto.ProductSaleShowDTO;
import com.erp.model.plm.dto.SkuDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.model.plm.enums.ProductSalesPlatformEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.server.plm.mapper.ProductSaleMapper;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductSaleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 产品销售信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 14:09
 **/
@Service
public class ProductSaleServiceImpl extends ServiceImpl<ProductSaleMapper, ProductSaleEntity> implements ProductSaleService {

    @Resource
    private ProductSaleMapper productSaleMapper;


    @Resource
    private BasicDictService basicDictService;

    /**
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductSaleShowDTO>
     * @Description 产品销售信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     **/
    @Override
    public List<ProductSaleShowDTO> list(String productId) {
        List<ProductSaleShowDTO> list = productSaleMapper.list(productId);
        for (ProductSaleShowDTO productSaleShowDTO : list) {
            if (StringUtils.isNotBlank(productSaleShowDTO.getSaleCountry())) {
                List<String> saleCountryList = Arrays.asList(productSaleShowDTO.getSaleCountry().split(","));
                List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(saleCountryList);
                List<String> saleCountryNameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                productSaleShowDTO.setSaleCountryName(StringUtils.join(saleCountryNameList, ","));
            }
        }
        return list;
    }

    /**
     * @param skuId
     * @return java.util.List<com.erp.model.plm.dto.ProductSaleShowDTO>
     * @Description 产品销售信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     **/
    @Override
    public List<ProductSaleShowDTO> listBySkuId(String skuId) {
        List<ProductSaleShowDTO> list = productSaleMapper.listBySkuId(skuId);
        for (ProductSaleShowDTO productSaleShowDTO : list) {
            if (StringUtils.isNotBlank(productSaleShowDTO.getSaleCountry())) {
                List<String> saleCountryList = Arrays.asList(productSaleShowDTO.getSaleCountry().split(","));
                List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(saleCountryList);
                List<String> saleCountryNameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                productSaleShowDTO.setSaleCountryName(StringUtils.join(saleCountryNameList, ","));
            }
        }
        return list;
    }

    /**
     * @param productSaleDTO 产品销售信息表请求参数
     * @return java.lang.Boolean
     * @Description 保存/修改产品销售信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     **/
    @Override
    public Boolean saveOrUpdate(ProductSaleDTO productSaleDTO) {
        ProductSaleEntity saleEntity = new ProductSaleEntity();
        BeanMapper.copy(productSaleDTO, saleEntity);
        return this.saveOrUpdate(saleEntity);
    }

    /**
     * @param productSaleList 产品销售信息表请求参数
     * @return java.lang.Boolean
     * @Description 保存/修改产品销售信息-批量操作
     * @Author Luo_WG
     * @Date 2022/9/26 18:11
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductSaleDTO> productSaleList) {
        List<ProductSaleEntity> list = BeanMapper.copyList(productSaleList, ProductSaleEntity.class);
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @param skuIds 产品sku明细表id
     * @return java.lang.Boolean
     * @Description 删除产品销售信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     **/
    @Override
    public Boolean removeSale(List<String> skuIds) {
        LambdaQueryWrapper<ProductSaleEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductSaleEntity::getSkuId, skuIds);
        return this.remove(queryWrapper);
    }

    @Override
    public ProductSaleEntity getBySkuId(String skuId) {
        LambdaQueryWrapper<ProductSaleEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductSaleEntity::getSkuId, skuId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public List<ProductSaleEntity> listBySkuIds(List<String> skuIds) {
        LambdaQueryWrapper<ProductSaleEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductSaleEntity::getSkuId, skuIds);
        return this.list(queryWrapper);
    }

    /**
     * 获取所有上市时间
     *
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     * @Author Luo_WG
     * @Date 2023/4/19 16:12
     **/
    @Override
    public List<NewProductDTO> getListingProductAll(Boolean sign) {
        return baseMapper.getListingProductAll(sign);
    }

    /**
     * 获取sku 销售信息
     *
     * @param skuNoList
     * @return
     */
    @Override
    public List<SkuDTO.SalesDTO> listSkuSalesBySkuNos(List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        List<SkuDTO.SalesDTO> list = baseMapper.listSkuSalesBySkuNos(skuNoList);
        for (SkuDTO.SalesDTO item : list) {
            Integer saleState = item.getSaleState();
            String saleStateName = SaleStateEnum.getNameByCode(saleState);
            item.setSaleStateName(saleStateName);
        }
        return list;
    }

    @Override
    public Boolean updateProductSaleListingTimeBatch(List<ProductSaleEntity> list) {
        for (ProductSaleEntity productSaleEntity : list) {
            lambdaUpdate()
                    .set(ProductSaleEntity::getListingTime, productSaleEntity.getListingTime())
                    .eq(ProductSaleEntity::getSkuId, productSaleEntity.getSkuId())
                    .update();
        }
        return Boolean.TRUE;
    }
}




