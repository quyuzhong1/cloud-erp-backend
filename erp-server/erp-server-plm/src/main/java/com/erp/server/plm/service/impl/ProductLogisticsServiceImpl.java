package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.plm.mapper.ProductLogisticsMapper;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductLogisticsService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    /**
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductLogisticsShowDTO>
     * @Description 产品物流信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     **/
    @Override
    public List<ProductLogisticsShowDTO> list(String productId) {
        return productLogisticsMapper.list(productId);
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
        return productLogisticsMapper.listBySkuId(skuId);
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
        return this.saveOrUpdate(logisticsEntity);
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
        return this.saveOrUpdateBatch(list);
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
            productLogisticDTO.setDeclareUnit(declareUnitMap.get(productLogisticDTO.getDeclareUnit()));
            productLogisticDTO.setDeclareCurrencyName(CurrencyEnum.getNameByCode(productLogisticDTO.getDeclareCurrency()));
            productLogisticDTO.setSourceCountryName(sourceCountryMap.get(productLogisticDTO.getSourceCountry()));
            for(ProductDetailDTO.ProductLogisticDTO child : productLogisticDTO.getChildList()){
                child.setDeclareUnit(declareUnitMap.get(productLogisticDTO.getDeclareUnit()));
                child.setDeclareCurrencyName(CurrencyEnum.getNameByCode(productLogisticDTO.getDeclareCurrency()));
                child.setSourceCountryName(sourceCountryMap.get(productLogisticDTO.getSourceCountry()));
            }
        }
        return productLogisticDTOList;
    }

}




