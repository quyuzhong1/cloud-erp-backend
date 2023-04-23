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
import com.erp.model.plm.dto.ProductSaleDTO;
import com.erp.model.plm.dto.ProductSaleShowDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.plm.mapper.ProductSaleMapper;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductSaleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ProductDetailService productDetailService;


    @Resource
    private MQProducerService mQProducerService;

    /**
     * @Description 产品销售信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductSaleShowDTO>
     **/
    @Override
    public List<ProductSaleShowDTO> list(String productId){
        return productSaleMapper.list(productId);
    }

    /**
     * @Description 保存/修改产品销售信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productSaleDTO 产品销售信息表请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductSaleDTO productSaleDTO){
        LocalDate pastListingTime = null;
        LocalDate newListingTime = null;
        ProductSaleEntity saleEntity = new ProductSaleEntity();
        BeanMapper.copy(productSaleDTO, saleEntity);
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(productSaleDTO.getId())) {
                saleEntity.setCreateUserId(loginUser.getUid());
                saleEntity.setCreateUserName(loginUser.getUserName());
            } else {
                saleEntity.setUpdateUserId(loginUser.getUid());
                saleEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
        if (StringUtils.isNotBlank(saleEntity.getId())) {
            ProductSaleEntity productSaleEntity = this.getById(saleEntity.getId());
            pastListingTime = productSaleEntity.getListingTime();
        }

        boolean flag = this.saveOrUpdate(saleEntity);
        newListingTime = productSaleDTO.getListingTime();
        if (flag) {
            if (!pastListingTime.equals(newListingTime)) {
                ProductDetailEntity productDetailEntity = productDetailService.getById(saleEntity.getSkuId());
                Map<String,Object> map = new HashMap<>();
                map.put("skuNo",productDetailEntity.getSkuNo());
                map.put("pastListingTime",pastListingTime);
                map.put("newListingTime",newListingTime);
                mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_LISTING_TAG.getName(), map, saleEntity.getId());
            }
        }
        return flag;
    }

    /**
     * @Description 保存/修改产品销售信息-批量操作
     * @Author Luo_WG
     * @Date 2022/9/26 18:11
     * @param productSaleList 产品销售信息表请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductSaleDTO> productSaleList){
        LocalDate pastListingTime = null;
        LocalDate newListingTime = null;
        List<ProductSaleEntity> list = BeanMapper.copyList(productSaleList, ProductSaleEntity.class);
        List<ProductSaleEntity> productSaleEntities = this.listByIds(list);
        boolean flag = this.saveOrUpdateBatch(list);

        if (flag) {
            for (ProductSaleEntity req : list) {
                newListingTime = req.getListingTime();

                ProductSaleEntity productSaleEntity = productSaleEntities.stream().filter(obj -> obj.getSkuId().equals(req.getSkuId())).findFirst().orElse(new ProductSaleEntity());
                pastListingTime = productSaleEntity.getListingTime();
                if (!newListingTime.equals(pastListingTime)) {
                    ProductDetailEntity productDetailEntity = productDetailService.getById(req.getSkuId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("skuNo", productDetailEntity.getSkuNo());
                    map.put("pastListingTime", pastListingTime);
                    map.put("newListingTime", newListingTime);
                    mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_LISTING_TAG.getName(), map, req.getId());
                }
            }

        }
        return flag;
    }

    /**
     * @Description 删除产品销售信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     * @param skuId 产品sku明细表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean removeSale(String skuId) {
        LambdaQueryWrapper<ProductSaleEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductSaleEntity::getSkuId, skuId);
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
}




