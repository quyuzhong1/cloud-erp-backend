package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.plm.mapper.ProductLogisticsMapper;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductLogisticsService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 产品物流信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 15:35
 **/
@Service
public class ProductLogisticsServiceImpl extends ServiceImpl<ProductLogisticsMapper, ProductLogisticsEntity>
        implements ProductLogisticsService {

    @Resource
    private ProductLogisticsMapper productLogisticsMapper;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private SysDictFeign sysDictFeign;
    @Autowired
    private BomInfoService bomInfoService;

    @Resource
    private ProductLogisticsService service;

    /**
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductLogisticsShowDTO>
     * @Description 产品物流信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     **/
    @Override
    public List<ProductLogisticsShowDTO> list(String productId) {
        List<ProductLogisticsShowDTO> list = productLogisticsMapper.list(productId);
        if (CollUtil.isNotEmpty(list)) {
            // 获取保险属性字典数据并缓存
            List<BasicDictEntity> dictList = basicDictService.listByType(BasicDictTypeEnum.INSURANCE_PROPERTY.getCode());
            Map<String, BasicDictEntity>  insurancePropertyMap = dictList.stream()
                    .collect(Collectors.toMap(BasicDictEntity::getValue, entity -> entity));

            list.forEach(item -> {
                if(StringUtils.isNotBlank(item.getInsuranceProperty())){
                    item.setInsurancePropertyList(Arrays.asList(item.getInsuranceProperty().split(",")));
                    item.setInsurancePropertyNameList(getInsurancePropertyList(item.getInsuranceProperty(), insurancePropertyMap));
                }
            });
        }
        return list;
    }

    @Override
    public List<String> getInsurancePropertyList(String insurancePropertyValue, Map<String, BasicDictEntity> mapById) {
        if(StringUtils.isBlank(insurancePropertyValue)){
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
     * @param skuId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductLogisticsShowDTO>
     * @Description 产品物流信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     **/
    @Override
    public List<ProductLogisticsShowDTO> listBySkuId(String skuId) {
        // 验证和清理 skuId，防止 SQL 注入
        if (StringUtils.isBlank(skuId)) {
            return Collections.emptyList();
        }
        List<ProductLogisticsShowDTO> productLogisticsShowDTOS = productLogisticsMapper.listBySkuId(skuId);

        if (CollUtil.isNotEmpty(productLogisticsShowDTOS)) {
            // 获取保险属性字典数据并缓存
            List<BasicDictEntity> dictList = basicDictService.listByType(BasicDictTypeEnum.INSURANCE_PROPERTY.getCode());
            Map<String, BasicDictEntity>  insurancePropertyMap = dictList.stream()
                    .collect(Collectors.toMap(BasicDictEntity::getValue, entity -> entity));

            productLogisticsShowDTOS.forEach(item -> {
                if(StringUtils.isNotBlank(item.getInsuranceProperty())){
                    item.setInsurancePropertyList(Arrays.asList(item.getInsuranceProperty().split(",")));
                    item.setInsurancePropertyNameList(getInsurancePropertyList(item.getInsuranceProperty(), insurancePropertyMap));
                }
            });
        }
        return productLogisticsShowDTOS;
    }



    /**
     * @param productLogisticsDTO 产品物流信息表
     * @return java.lang.Boolean
     * @Description 保存/修改产品物流信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     **/
    @Override
    public Boolean saveOrUpdate(ProductLogisticsDTO productLogisticsDTO) {
        ProductLogisticsEntity logisticsEntity = new ProductLogisticsEntity();
        BeanMapper.copy(productLogisticsDTO, logisticsEntity);
        //物流属性
        if (StrUtil.isNotBlank(productLogisticsDTO.getProductPropertyId())) {
            List<BasicDictEntity> propertytList = basicDictService.listByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());
            List<String> propertyIdList = Arrays.stream(productLogisticsDTO.getProductPropertyId().split(",")).collect(Collectors.toList());
            String propertyNames = propertytList.stream().filter(obj -> propertyIdList.contains(obj.getId())).map(BasicDictEntity::getName).collect(Collectors.joining(","));
            logisticsEntity.setProductProperty(propertyNames);
        }
        //根据sku获取物流产品信息记录
        if (StringUtils.isNotBlank(productLogisticsDTO.getSkuId())){
            List<ProductLogisticsEntity> list = this.lambdaQuery().eq(ProductLogisticsEntity::getSkuId, productLogisticsDTO.getSkuId()).list();
            if (CollectionUtils.isNotEmpty(list)){
                logisticsEntity.setId(list.get(0).getId());
            }
        }
        boolean result = service.saveOrUpdate(logisticsEntity);
        this.saveOrUpdateParentPropertyIdByChildSkuId(Arrays.asList(logisticsEntity.getSkuId()));
        return result;
    }



    /**
     * @param productLogisticsList 产品物流信息表
     * @return java.lang.Boolean
     * @Description 保存/修改产品物流信息-批量操作
     * @Author Luo_WG
     * @Date 2022/9/26 18:15
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductLogisticsDTO> productLogisticsList) {
        List<ProductLogisticsEntity> list = BeanMapper.copyList(productLogisticsList, ProductLogisticsEntity.class);
        List<String> skuIdList = list.stream().map(ProductLogisticsEntity::getSkuId).collect(Collectors.toList());
        List<ProductLogisticsEntity> oldList = this.lambdaQuery().in(ProductLogisticsEntity::getSkuId, skuIdList).list();
        //物流属性
        List<BasicDictEntity> propertytList = basicDictService.listByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());
        for (ProductLogisticsEntity productLogisticsEntity : list) {
            //物流属性名称
            if (StrUtil.isNotBlank(productLogisticsEntity.getProductPropertyId())) {
                List<String> propertyIdList = Arrays.stream(productLogisticsEntity.getProductPropertyId().split(",")).collect(Collectors.toList());
                String propertyNames = propertytList.stream().filter(obj -> propertyIdList.contains(obj.getId())).map(BasicDictEntity::getName).collect(Collectors.joining(","));
                productLogisticsEntity.setProductProperty(propertyNames);
            }
            //根据sku获取产品物流信息记录
            if (CollectionUtils.isNotEmpty(skuIdList) && CollectionUtils.isNotEmpty(oldList)){
                ProductLogisticsEntity logisticsEntity = oldList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(productLogisticsEntity.getSkuId())).findFirst().orElse(new ProductLogisticsEntity());
                productLogisticsEntity.setId(logisticsEntity.getId());
            }
        }
        boolean result = service.saveOrUpdateBatch(list);
        this.saveOrUpdateParentPropertyIdByChildSkuId(skuIdList);
        return result;
    }

    /**
     * @param skuIds 产品sku明细表id
     * @return java.lang.Boolean
     * @Description 删除产品物流信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     **/
    @Override
    public Boolean removeLogistics(List<String> skuIds) {
        LambdaQueryWrapper<ProductLogisticsEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductLogisticsEntity::getSkuId, skuIds);
        return this.remove(queryWrapper);
    }

    @Override
    public ProductLogisticsEntity getBySkuId(String skuId) {
        LambdaQueryWrapper<ProductLogisticsEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductLogisticsEntity::getSkuId, skuId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public List<ProductLogisticsEntity> listBySkuIdList(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(ProductLogisticsEntity::getSkuId,skuIdList).list();
    }

    @Override
    public List<LogisticsProductDTO.SelectDTO> selectSku() {
        return baseMapper.selectSku();
    }

    @Override
    public ProductLogisticsEntity getEntityById(String id) {
        return baseMapper.getEntityById(id);
    }

    @Override
    public List<ProductDetailDTO.ProductLogisticDTO> listProductLogisticsByIds(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        List<ProductDetailDTO.ProductLogisticDTO> productLogisticDTOList = baseMapper.listProductLogisticsByIds(skuIdList);
        if(CollectionUtils.isEmpty(productLogisticDTOList)){
            return  Collections.emptyList();
        }
        //先处理组合品情况
        List<String> skuIds = productLogisticDTOList.stream().map(ProductDetailDTO.ProductLogisticDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> allBomChildrenSkuDTOS = bomSkuService.listBomChildBySkuIds(skuIds);
        List<String> childrenSkuIds = allBomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailDTO.ProductLogisticDTO> allChildList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(childrenSkuIds)){
            allChildList = baseMapper.listProductLogisticsByIds(childrenSkuIds);
        }
        for (ProductDetailDTO.ProductLogisticDTO productLogisticDTO : productLogisticDTOList) {
            List<BomChildrenSkuDTO> bomChildrenSkuDTOList = allBomChildrenSkuDTOS.stream().filter(v->v.getParentSkuId().equals(productLogisticDTO.getSkuId())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(bomChildrenSkuDTOList)){
                Map<String,Integer> bomchildMap = bomChildrenSkuDTOList.stream().collect(Collectors.toMap(BomChildrenSkuDTO::getSkuId,BomChildrenSkuDTO::getQuantity,(v1,v2)->v1));
                productLogisticDTO.setIsCombination(true);
                List<ProductDetailDTO.ProductLogisticDTO> childList = allChildList.stream().filter(v->bomchildMap.containsKey(v.getSkuId())).collect(Collectors.toList());
                childList.forEach(v->{
                    v.setChildQty(bomchildMap.getOrDefault(v.getSkuId(),1));
                });
                productLogisticDTO.setChildList(childList);
            }else{
                productLogisticDTO.setIsCombination(false);
            }
        }
        //处理中文名
        Map<String,String> declareUnitMap = basicDictService.mapByType("declareUnit");
        List<String> sourceCountryIdList = productLogisticDTOList.stream().map(ProductDetailDTO.ProductLogisticDTO::getSourceCountry).collect(Collectors.toList());
        List<DictCountryEntity> sourceCountryList = sysDictFeign.listCountryByIds(sourceCountryIdList);
        Map<String,String> sourceCountryMap = sourceCountryList.stream().collect(Collectors.toMap(DictCountryEntity::getId,DictCountryEntity::getNameCn,(v1,v2)->v1));
        for (ProductDetailDTO.ProductLogisticDTO productLogisticDTO : productLogisticDTOList) {
            productLogisticDTO.setDeclareUnitName(declareUnitMap.get(productLogisticDTO.getDeclareUnit()));
            productLogisticDTO.setDeclareCurrencyName(CurrencyEnum.getNameByCode(productLogisticDTO.getDeclareCurrency()));
            productLogisticDTO.setSourceCountryName(sourceCountryMap.get(productLogisticDTO.getSourceCountry()));
            for(ProductDetailDTO.ProductLogisticDTO child : productLogisticDTO.getChildList()){
                child.setDeclareUnitName(declareUnitMap.get(child.getDeclareUnit()));
                child.setDeclareCurrencyName(CurrencyEnum.getNameByCode(child.getDeclareCurrency()));
                child.setSourceCountryName(sourceCountryMap.get(child.getSourceCountry()));
            }
        }
        return productLogisticDTOList;
    }

    @Override
    public Boolean saveOrUpdateParentPropertyIdByChildSkuId(List<String> childSkuIds) {
        if(CollectionUtils.isEmpty(childSkuIds)){
            return true;
        }
        List<BomSkuEntity> bomSkuEntityList = bomSkuService.listByChildSkuIds(childSkuIds);
        List<String> parentSkuIds = bomSkuEntityList.stream().map(BomSkuEntity::getParentSkuId).distinct().collect(Collectors.toList());
        return saveOrUpdateParentPropertyId(parentSkuIds);
    }

    @Override
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

        List<ProductLogisticsEntity> productLogisticsList = this.listBySkuIdList(parentSkuIds);
        List<String> childrenSkuIds = bomSkuEntityList.stream().map(BomSkuEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductLogisticsEntity> allChildProductLogisticsEntityList = this.listBySkuIdList(childrenSkuIds);

        //物流属性
        List<BasicDictEntity> propertytList = basicDictService.listByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());

        List<ProductLogisticsEntity> updateList = new ArrayList<>();
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
            ProductLogisticsEntity productLogisticsEntity = productLogisticsList.stream().filter(v->v.getSkuId().equals(parentSkuId)).findAny().orElse(null);
            if(Objects.isNull(productLogisticsEntity)){
                continue;
            }
            List<ProductLogisticsEntity> childProductLogisticsEntityList = allChildProductLogisticsEntityList.stream().filter(v->childSkuIds.contains(v.getSkuId())).collect(Collectors.toList());
            //属性默认取子件SKU的属性合集
            List<String> childrenLPropertyIds = childProductLogisticsEntityList.stream().map(ProductLogisticsEntity::getProductPropertyId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            String propertyIds = childrenLPropertyIds.stream()
                    .flatMap(id -> Arrays.stream(id.split(",")))
                    .distinct()
                    .collect(Collectors.joining(","));
            if(StringUtils.isNotBlank(propertyIds)){
                String productProperty = propertytList.stream().filter(obj -> childrenLPropertyIds.contains(obj.getId())).map(BasicDictEntity::getName).collect(Collectors.joining(","));
                productLogisticsEntity.setProductProperty(productProperty);
                //物流属性名称
                productLogisticsEntity.setProductPropertyId(propertyIds);
                updateList.add(productLogisticsEntity);
            }
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            service.updateBatchById(updateList);
        }
        return true;
    }

}




