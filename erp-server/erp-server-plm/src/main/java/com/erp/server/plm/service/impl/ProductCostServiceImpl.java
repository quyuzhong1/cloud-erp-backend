package com.erp.server.plm.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.BeanMapper;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.dto.ProductPurchaseShowDTO;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.plm.mapper.ProductCostMapper;
import com.erp.server.plm.service.ProductCostService;
import com.erp.server.plm.service.ProductPurchaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 产品成本信息服务类
 * @Author Luo_WG
 * @Date 2022/9/22 16:15
 **/
@Service
public class ProductCostServiceImpl extends ServiceImpl<ProductCostMapper, ProductCostEntity> implements ProductCostService {

    @Resource
    private ProductCostMapper productCostMapper;

    @Resource
    private RedisUtil redisUtil;



    /**
     * @Description 产品成本信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    @Override
    public List<ProductCostShowDTO> list(String productId) {
        List<ProductCostShowDTO> productCostShowDTOList = productCostMapper.list(productId);
        if (CollectionUtils.isNotEmpty(productCostShowDTOList)) {
            for (ProductCostShowDTO productCostShowDTO : productCostShowDTOList) {
                String existKey = StrUtil.format(RedisKeyConstant.DMP_SKU_COST_CODE, productCostShowDTO.getSkuNo());
                DmpSkuCostEntity dmpSkuCostEntity = (DmpSkuCostEntity) redisUtil.get(existKey);
                BigDecimal purchasePrice = BigDecimal.ZERO;
                if (ObjectUtil.isEmpty(dmpSkuCostEntity)) {
                    purchasePrice = dmpSkuCostEntity.getCostPrice();
                }
                //含税单价
                productCostShowDTO.setActualTaxCost(purchasePrice);
                //不含税单价
                BigDecimal actualNotTaxCost = purchasePrice.divide(MathUtil.BigDecimal_1.add(productCostShowDTO.getTaxRate()), 4, BigDecimal.ROUND_DOWN);
                productCostShowDTO.setActualNoTaxCost(actualNotTaxCost);
            }
        }
        return productCostShowDTOList;
    }


    /**
     * @Description 根据skuId查询产品成本信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param skuId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    @Override
    public List<ProductCostShowDTO> listBySkuId(String skuId) {
        List<ProductCostShowDTO> productCostShowDTOList = productCostMapper.listBySkuId(skuId);
        if (CollectionUtils.isNotEmpty(productCostShowDTOList)) {
            for (ProductCostShowDTO productCostShowDTO : productCostShowDTOList) {
                String existKey = StrUtil.format(RedisKeyConstant.DMP_SKU_COST_CODE, productCostShowDTO.getSkuNo());
                DmpSkuCostEntity dmpSkuCostEntity = (DmpSkuCostEntity) redisUtil.get(existKey);
                BigDecimal purchasePrice = BigDecimal.ZERO;
                if (ObjectUtil.isEmpty(dmpSkuCostEntity)) {
                    purchasePrice = dmpSkuCostEntity.getCostPrice();
                }
                //含税单价
                productCostShowDTO.setActualTaxCost(purchasePrice);
                //不含税单价
                BigDecimal actualNotTaxCost = purchasePrice.divide(MathUtil.BigDecimal_1.add(productCostShowDTO.getTaxRate()), 4, BigDecimal.ROUND_DOWN);
                productCostShowDTO.setActualNoTaxCost(actualNotTaxCost);
            }
        }
        return productCostShowDTOList;
    }

    /**
     * @Description 保存/修改产品成本信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productCostDTO 产品成本信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductCostDTO productCostDTO) {
        ProductCostEntity costEntity = new ProductCostEntity();
        BeanMapper.copy(productCostDTO, costEntity);
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(productCostDTO.getId())) {
                costEntity.setCreateUserId(loginUser.getUid());
                costEntity.setCreateUserName(loginUser.getUserName());
            } else {
                costEntity.setUpdateUserId(loginUser.getUid());
                costEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
        return this.saveOrUpdate(costEntity);
    }

    /**
     * @Description 保存/修改产品成本信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 18:05
     * @param productCostList 产品成本信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductCostDTO> productCostList) {
        List<ProductCostEntity> list = BeanMapper.copyList(productCostList, ProductCostEntity.class);
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @Description 删除产品采购信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     * @param skuIds 产品sku明细表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean removeCost(List<String> skuIds) {
        LambdaQueryWrapper<ProductCostEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductCostEntity::getSkuId, skuIds);
        return this.remove(queryWrapper);
    }

    @Override
    public ProductCostEntity getBySkuId(String skuId) {
        LambdaQueryWrapper<ProductCostEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductCostEntity::getSkuId, skuId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }
}




