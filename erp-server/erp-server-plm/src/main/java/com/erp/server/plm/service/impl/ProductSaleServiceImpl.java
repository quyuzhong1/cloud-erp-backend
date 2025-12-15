package com.erp.server.plm.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.NewProductDTO;
import com.erp.model.plm.dto.ProductSaleDTO;
import com.erp.model.plm.dto.ProductSaleShowDTO;
import com.erp.model.plm.dto.SkuDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.plm.mapper.ProductSaleMapper;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductSaleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    private BomSkuService bomSkuService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private SysDictFeign sysDictFeign;


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
        // 获取保险属性字典数据并缓存
        List<BasicDictEntity> dictList = basicDictService.listByType(BasicDictTypeEnum.INSURANCE_PROPERTY.getCode());
        Map<String, BasicDictEntity>  insurancePropertyMap = dictList.stream()
                .collect(Collectors.toMap(BasicDictEntity::getValue, entity -> entity));
        for (ProductSaleShowDTO productSaleShowDTO : list) {

            if(StringUtils.isNotBlank(productSaleShowDTO.getInsuranceProperty())){
                productSaleShowDTO.setInsurancePropertyList(Arrays.asList(productSaleShowDTO.getInsuranceProperty().split(",")));
                productSaleShowDTO.setInsurancePropertyNameList(getInsurancePropertyList(productSaleShowDTO.getInsuranceProperty(), insurancePropertyMap));
            }
            if (StringUtils.isNotBlank(productSaleShowDTO.getSaleCountry())) {
                List<String> saleCountryList = Arrays.asList(productSaleShowDTO.getSaleCountry().split(","));
                List<DictCountryEntity> dictCountryEntities = sysDictFeign.listCountryByIds(saleCountryList);
                List<String> saleCountryNameList = dictCountryEntities.stream().map(DictCountryEntity::getNameCn).collect(Collectors.toList());
                productSaleShowDTO.setSaleCountryName(StringUtils.join(saleCountryNameList, ","));
            }
        }
        return list;
    }

    @Override
    public List<String> getInsurancePropertyList(String insurancePropertyValue, Map<String, BasicDictEntity> mapById) {
        if(com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(insurancePropertyValue)){
            return Collections.emptyList();
        }
        List<String> insurancePropertyNsameList = new ArrayList<>();
        if(insurancePropertyValue.contains(",")) {
            String[] split = insurancePropertyValue.split(",");
            for (String s : split) {
                BasicDictEntity entity = mapById.get(s.trim());
                if (Objects.nonNull(entity)) {
                    insurancePropertyNsameList.add(entity.getName());
                }
            }
        }else {
            BasicDictEntity entity = mapById.get(insurancePropertyValue.trim());
            if (Objects.nonNull(entity)) {
                insurancePropertyNsameList.add(entity.getName());
            }
        }
        return insurancePropertyNsameList;
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

        // 获取保险属性字典数据并缓存
        List<BasicDictEntity> dictList = basicDictService.listByType(BasicDictTypeEnum.INSURANCE_PROPERTY.getCode());
        Map<String, BasicDictEntity>  insurancePropertyMap = dictList.stream()
                .collect(Collectors.toMap(BasicDictEntity::getValue, entity -> entity));

        for (ProductSaleShowDTO productSaleShowDTO : list) {
            if(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(productSaleShowDTO.getInsuranceProperty())){
                productSaleShowDTO.setInsurancePropertyList(Arrays.asList(productSaleShowDTO.getInsuranceProperty().split(",")));
                productSaleShowDTO.setInsurancePropertyNameList(getInsurancePropertyList(productSaleShowDTO.getInsuranceProperty(), insurancePropertyMap));
            }
            if (StringUtils.isNotBlank(productSaleShowDTO.getSaleCountry())) {
                List<String> saleCountryList = Arrays.asList(productSaleShowDTO.getSaleCountry().split(","));
                List<DictCountryEntity> dictCountryEntities = sysDictFeign.listCountryByIds(saleCountryList);
                List<String> saleCountryNameList = dictCountryEntities.stream().map(DictCountryEntity::getNameCn).collect(Collectors.toList());
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
        //物流属性
        if (StrUtil.isNotBlank(productSaleDTO.getProductPropertyId())) {
            List<BasicDictEntity> propertytList = basicDictService.listByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());
            List<String> propertyIdList = Arrays.stream(productSaleDTO.getProductPropertyId().split(",")).collect(Collectors.toList());
            String propertyNames = propertytList.stream().filter(obj -> propertyIdList.contains(obj.getId())).map(BasicDictEntity::getName).collect(Collectors.joining(","));
            saleEntity.setProductProperty(propertyNames);
        }
        //处理数据
        handleSaveOrUpdate(saleEntity);
        this.saveOrUpdateParentPropertyIdByChildSkuId(Arrays.asList(productSaleDTO.getSkuId()));
        return this.saveOrUpdate(saleEntity);
    }

    /**
     * 数据处理
     * @author will
     * @date 2025/5/13 15:11
     * @param entity
     * @return void
     */
    private void handleSaveOrUpdate (ProductSaleEntity entity) {
        ProductSaleEntity oldEntity = this.getBySkuId(entity.getSkuId());
        if (ObjUtil.isEmpty(oldEntity)) {
            return;
        }
        entity.setId(oldEntity.getId());
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
        //根据sku查询
        List<String> skuIdList = list.stream().map(ProductSaleEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductSaleEntity> oldList = this.listBySkuIds(skuIdList);
        Map<String, String> map = oldList.stream().collect(Collectors.toMap(ProductSaleEntity::getSkuId, ProductSaleEntity::getId));
        //物流属性
        List<BasicDictEntity> propertytList = basicDictService.listByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());
        for (ProductSaleEntity productSaleEntity : list) {
            //物流属性名称
            if (StrUtil.isNotBlank(productSaleEntity.getProductPropertyId())) {
                List<String> propertyIdList = Arrays.stream(productSaleEntity.getProductPropertyId().split(",")).collect(Collectors.toList());
                String propertyNames = propertytList.stream().filter(obj -> propertyIdList.contains(obj.getId())).map(BasicDictEntity::getName).collect(Collectors.joining(","));
                productSaleEntity.setProductProperty(propertyNames);
            }
        }
        for (ProductSaleEntity entity : list) {
            entity.setId(map.get(entity.getSkuId()));
        }
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

    @Override
    public List<ProductSaleEntity> listBySkuIdList(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(ProductSaleEntity::getSkuId,skuIdList).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateParentPropertyIdByChildSkuId(List<String> childSkuIds) {
        if(CollectionUtils.isEmpty(childSkuIds)){
            return true;
        }
        List<BomSkuEntity> bomSkuEntityList = bomSkuService.listByChildSkuIds(childSkuIds);
        List<String> parentSkuIds = bomSkuEntityList.stream().map(BomSkuEntity::getParentSkuId).distinct().collect(Collectors.toList());
        return saveOrUpdateParentPropertyId(parentSkuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateParentPropertyId(List<String> parentSkuIds) {
        if(CollectionUtils.isEmpty(parentSkuIds)){
            return false;
        }
        List<BomSkuEntity> bomSkuEntityList = bomSkuService.listByParentSkuIds(parentSkuIds);
        if(CollectionUtils.isEmpty(bomSkuEntityList)){
            return false;
        }
        List<String> allBomIds = bomSkuEntityList.stream().map(BomSkuEntity::getBomId).distinct().collect(Collectors.toList());
        List<BomInfoEntity> bomInfoEntityList = bomInfoService.listByIds(allBomIds);

        List<ProductSaleEntity> productLogisticsList = this.listBySkuIdList(parentSkuIds);
        List<String> childrenSkuIds = bomSkuEntityList.stream().map(BomSkuEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductSaleEntity> allChildProductSaleEntityList = this.listBySkuIdList(childrenSkuIds);

        //物流属性
        List<BasicDictEntity> propertytList = basicDictService.listByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());

        List<ProductSaleEntity> updateList = new ArrayList<>();
        for (BomInfoEntity bomInfoEntity : bomInfoEntityList) {
            if(!BomTypeEnum.COMBINATION.getType().equals(bomInfoEntity.getType())){
                continue;
            }
            List<BomSkuEntity> bomSkuEntitys = bomSkuEntityList.stream().filter(v->v.getBomId().equals(bomInfoEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(bomSkuEntitys)){
                continue;
            }
            String parentSkuId = bomSkuEntitys.get(0).getParentSkuId();
            List<String> childSkuIds = bomSkuEntitys.stream().map(BomSkuEntity::getSkuId).distinct().collect(Collectors.toList());
            ProductSaleEntity productSaleEntity = productLogisticsList.stream().filter(v->v.getSkuId().equals(parentSkuId)).findAny().orElse(null);
            if(Objects.isNull(productSaleEntity)){
                continue;
            }
            List<ProductSaleEntity> childProductSaleEntityList = allChildProductSaleEntityList.stream().filter(v->childSkuIds.contains(v.getSkuId())).collect(Collectors.toList());
            //属性默认取子件SKU的属性合集
            List<String> childrenLPropertyIds = childProductSaleEntityList.stream().map(ProductSaleEntity::getProductPropertyId).filter(com.baomidou.mybatisplus.core.toolkit.StringUtils::isNotBlank).collect(Collectors.toList());
            String propertyIds = childrenLPropertyIds.stream()
                    .flatMap(id -> Arrays.stream(id.split(",")))
                    .distinct()
                    .collect(Collectors.joining(","));
            if(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(propertyIds)){
                String productProperty = propertytList.stream().filter(obj -> childrenLPropertyIds.contains(obj.getId())).map(BasicDictEntity::getName).collect(Collectors.joining(","));
                productSaleEntity.setProductProperty(productProperty);
                //物流属性名称
                productSaleEntity.setProductPropertyId(propertyIds);
                updateList.add(productSaleEntity);
            }
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        return true;
    }
}




