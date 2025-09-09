package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.dto.ProductPurchaseDTO;
import com.erp.model.plm.dto.ProductPurchaseShowDTO;
import com.erp.model.plm.dto.SkuPurchaseDTO;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.ProductPurchaseMapper;
import com.erp.server.plm.service.ProductPurchaseService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description 产品采购信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 11:23
 **/
@Service
public class ProductPurchaseServiceImpl extends ServiceImpl<ProductPurchaseMapper, ProductPurchaseEntity> implements ProductPurchaseService {

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private SupplierFeign supplierFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    /**
     * @Description 产品采购信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 11:48
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductPurchaseShowDTO>
     **/
    @Override
    public List<ProductPurchaseShowDTO> list(String productId) {
        return baseMapper.list(productId);
    }

    /**
     * @Description 产品采购信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 11:48
     * @param skuId
     * @return java.util.List<com.erp.model.plm.dto.ProductPurchaseShowDTO>
     **/
    @Override
    public List<ProductPurchaseShowDTO> listBySkuId(String skuId) {
        List<ProductPurchaseShowDTO> purchaseShowDTOList = baseMapper.listBySkuId(skuId);
        return purchaseShowDTOList;
    }

    /**
    * @Description 保存/修改产品采购信息
    * @Author Luo_WG
    * @Date 2022/9/23 11:48
    * @param purchaseDTO 产品采购信息表请求参数
    * @return java.lang.String
    **/
    @Override
    public Boolean saveOrUpdate(ProductPurchaseDTO purchaseDTO) {
        ProductPurchaseEntity purchaseEntity = new ProductPurchaseEntity();
        BeanMapper.copy(purchaseDTO, purchaseEntity);
        //验证数据
        checkProductPurchase(purchaseEntity);
        //处理数据
        handleSaveOrUpdate(purchaseEntity);
        return this.saveOrUpdate(purchaseEntity);
    }

    /**
     * 数据处理
     * @author will
     * @date 2025/5/13 15:11
     * @param entity
     * @return void
     */
    private void handleSaveOrUpdate (ProductPurchaseEntity entity) {
        ProductPurchaseEntity oldEntity = this.getBySkuId(entity.getSkuId());
        if (ObjUtil.isEmpty(oldEntity)) {
            return;
        }
        entity.setId(oldEntity.getId());
    }

    /**
     * @Description 保存/修改产品采购信息-批量
     * @Author Luo_WG
     * @Date 2022/9/26 18:05
     * @param purchaseList 产品成本信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductPurchaseDTO> purchaseList) {
        List<ProductPurchaseEntity> list = BeanMapper.copyList(purchaseList, ProductPurchaseEntity.class);

        //根据sku查询
        List<String> skuIdList = list.stream().map(ProductPurchaseEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductPurchaseEntity> oldList = this.listBySkuIds(skuIdList);
        Map<String, String> map = oldList.stream().collect(Collectors.toMap(ProductPurchaseEntity::getSkuId, ProductPurchaseEntity::getId));
        Map<String, List<ProductPurchaseEntity>> eanMap = list.stream().filter(obj -> StringUtils.isNotBlank(obj.getEan())).collect(Collectors.groupingBy(ProductPurchaseEntity::getEan));

        for (ProductPurchaseEntity entity : list) {
            //Id赋值
            entity.setId(map.get(entity.getSkuId()));

            //验证ean
            if (CharSequenceUtil.isNotBlank(entity.getEan())) {
                List<ProductPurchaseEntity> eanList = eanMap.get(entity.getEan());
                if (eanList.size() > 1) {
                    throw new ServiceException(ApiError.ERROR_95164);
                }
                //验证数据
                checkProductPurchase(eanList.get(0));
            }
        }
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
    public Boolean removePurchase(List<String> skuIds) {
        LambdaQueryWrapper<ProductPurchaseEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductPurchaseEntity::getSkuId, skuIds);
        return this.remove(queryWrapper);
    }

    @Override
    public ProductPurchaseEntity getBySkuId(String skuId) {
        LambdaQueryWrapper<ProductPurchaseEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductPurchaseEntity::getSkuId, skuId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public List<ProductPurchaseEntity> listBySkuIds(List<String> skuIds) {
        return lambdaQuery().in(ProductPurchaseEntity::getSkuId, skuIds).list();
    }

    /**
     * @description: 数据验证
     * @author Will
     * @date: 2023/5/11 16:20
     * @param entity
     */
    @Override
    public void checkProductPurchase (ProductPurchaseEntity entity) {
        //ean不能重复
        String ean = entity.getEan();
        if (StringUtils.isBlank(ean)) {
            return;
        }
        List<ProductPurchaseEntity> list = lambdaQuery().eq(ProductPurchaseEntity::getEan, ean).list();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> ids = list.stream().map(ProductPurchaseEntity::getId).collect(Collectors.toList());
            if (ids.size() > 1 || !ids.contains(entity.getId())) {
                throw new ServiceException(ApiError.ERROR_95164);
            }
        }
    }

    @Override
    public List<SkuPurchaseDTO.PurchaseInfo> getInfoBySkuIds(List<String> skuIds) {
        if(CollUtil.isEmpty(skuIds)) {
            return null;
        }
        List<ProductPurchaseEntity> productPurchaseList = lambdaQuery().in(ProductPurchaseEntity::getSkuId, skuIds).list();
        if(CollUtil.isEmpty(productPurchaseList)) {
            return null;
        }
        // 此处注意，实际发现某些sku存在多条采购信息
        Map<String, List<ProductPurchaseEntity>> skuPurchaseMap = productPurchaseList.stream().collect(Collectors.groupingBy(ProductPurchaseEntity::getSkuId));

        List<String> purchaseUserIds = productPurchaseList.stream().filter(r-> StrUtils.isNotEmpty(r.getPurchaseUserId())).map(ProductPurchaseEntity::getPurchaseUserId).distinct().collect(Collectors.toList());
        List<String> mainSupplierIds = productPurchaseList.stream().filter(r-> StrUtils.isNotEmpty(r.getMainSupplier())).map(ProductPurchaseEntity::getMainSupplier).distinct().collect(Collectors.toList());

        // 用户信息
        Map<String, FindUserDTO> userMap = Maps.newHashMap();
        List<FindUserDTO> userInfos = sysUserFeign.getUserListByUserIds(purchaseUserIds);
        if(CollUtil.isNotEmpty(userInfos)) {
            userMap = userInfos.stream().collect(Collectors.toMap(FindUserDTO::getUserId, Function.identity()));
        }

        // 供应商信息
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(mainSupplierIds);

        List<SkuPurchaseDTO.PurchaseInfo> resultList = Lists.newArrayListWithExpectedSize(skuIds.size());
        for(String skuId : skuIds) {
            List<ProductPurchaseEntity> skuPurchaseList = skuPurchaseMap.get(skuId);
            if(CollUtil.isNotEmpty(skuPurchaseList)) {
                ProductPurchaseEntity productPurchaseEntity = skuPurchaseList.get(0);
                SkuPurchaseDTO.PurchaseInfo purchaseInfo = new SkuPurchaseDTO.PurchaseInfo();
                purchaseInfo.setSkuId(skuId);
                purchaseInfo.setPurchaseUserId(productPurchaseEntity.getPurchaseUserId());
                purchaseInfo.setPurchaseUserName(userMap.getOrDefault(productPurchaseEntity.getPurchaseUserId(),new FindUserDTO()).getUserName());
                purchaseInfo.setSupplierId(productPurchaseEntity.getMainSupplier());
                purchaseInfo.setSupplierName(supplierMap.getOrDefault(productPurchaseEntity.getMainSupplier(), new SupplierDTO.SupplierSimpleDTO()).getName());
                resultList.add(purchaseInfo);
            }
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProductPlaceOrderTimeBatch(List<ProductPurchaseEntity> list) {
        for (ProductPurchaseEntity purchaseEntity : list) {
            lambdaUpdate()
                    .set(ProductPurchaseEntity::getPlaceOrderTime, purchaseEntity.getPlaceOrderTime())
                    .eq(ProductPurchaseEntity::getSkuId, purchaseEntity.getSkuId())
                    .update();
        }
        return Boolean.TRUE;
    }

    @Override
    public ProductPurchaseEntity getByEan(String ean) {
        LambdaQueryWrapper<ProductPurchaseEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductPurchaseEntity::getEan, ean);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public ProductDetailDTO.ServiceToWavePickingDTO getProductInfoBySkuId(String skuId) {
        return baseMapper.listWavePickingDTOBySkuIds(skuId);
    }

    @Override
    public List<ProductDetailDTO.SkuDTO> listSkuInfoByEanOrSkuNo(ProductDetailDTO.SearchDTO dto) {
        if (CharSequenceUtil.isBlank(dto.getSearchKeyword())) {
            return null;
        }
        List<ProductDetailDTO.SkuDTO> skuDTOS = baseMapper.listSkuInfoByEanOrSkuNo(dto);
        if (CollectionUtils.isEmpty(skuDTOS)) {
            return null;
        }
        //存在的已审核sku
        List<String> skuNoList = skuDTOS.stream().filter(e -> Objects.equals(e.getStatus(), ProductDetailStatusEnum.APPROVAL_PASS.getCode())).map(ProductDetailDTO.SkuDTO::getSkuNo).distinct().collect(Collectors.toList());
        String skuNOs = skuDTOS.stream().filter(e -> !Objects.equals(e.getStatus(), ProductDetailStatusEnum.APPROVAL_PASS.getCode()) && !skuNoList.contains(e.getSkuNo())).map(ProductDetailDTO.SkuDTO::getSkuNo).distinct().collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(skuNOs)){
            throw new ServiceException("扫码SKU【{}】未审核", skuNOs);
        }
        return skuDTOS.stream().filter( e -> Objects.equals(e.getStatus(), ProductDetailStatusEnum.APPROVAL_PASS.getCode())).collect(Collectors.toList());
    }
}




