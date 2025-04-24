package com.erp.server.plm.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.plm.mapper.ProductCostMapper;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductCostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description 产品成本信息服务类
 * @Author Luo_WG
 * @Date 2022/9/22 16:15
 **/
@Service
public class ProductCostServiceImpl extends ServiceImpl<ProductCostMapper, ProductCostEntity> implements ProductCostService {

    public static final String SKU_COST_SALE_ORG_ID = "skuCostSaleOrgId";
    public static final String SKU_COST_WAREHOUSE = "skuCostWarehouse";
    @Resource
    private ProductCostMapper productCostMapper;

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private BasicDictService basicDictService;



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
            List<String> skuNoList = productCostShowDTOList.stream().map(ProductCostShowDTO::getSkuNo).collect(Collectors.toList());
            List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
            //获取SKU成本
            List<String> skuIds = productCostShowDTOList.stream().map(ProductCostShowDTO::getSkuId).distinct().collect(Collectors.toList());
            List<BasicDictEntity> basicDictEntities = basicDictService.listByTypeList(Arrays.asList(SKU_COST_SALE_ORG_ID, SKU_COST_WAREHOUSE));
            String skuCostSaleOrgId = basicDictEntities.stream().filter(e -> e.getType().equals(SKU_COST_SALE_ORG_ID)).map(BasicDictEntity::getValue).findFirst().orElse("");
            String skuCostWarehouseId = basicDictEntities.stream().filter(e -> e.getType().equals(SKU_COST_WAREHOUSE)).map(BasicDictEntity::getValue).findFirst().orElse("");
            InventorySkuCostDTO.QueryB2BDTO queryB2BDTO = InventorySkuCostDTO.QueryB2BDTO.builder().salesOrgId(skuCostSaleOrgId).warehouseId(skuCostWarehouseId).skuIds(skuIds).billDate(LocalDate.now()).build();
            List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS = logisticsFeign.listSkuCostBySkuIds(queryB2BDTO);
            for (ProductCostShowDTO productCostShowDTO : productCostShowDTOList) {
                //成本信息
                DmpSkuCostEntity dmpSkuCostEntity = dmpSkuCostList.stream().filter(obj -> obj.getSkuNo().equals(productCostShowDTO.getSkuNo())).findFirst().orElse(null);
                BigDecimal actualTaxCost = BigDecimal.ZERO;
                BigDecimal actualNoTaxCost = BigDecimal.ZERO;
                if (Objects.nonNull(dmpSkuCostEntity)) {
                    actualTaxCost = dmpSkuCostEntity.getCostPrice();
                    actualNoTaxCost = dmpSkuCostEntity.getNotTaxCostPrice();
                }
                //SKU成本
                InventorySkuCostDTO.SkuCostDTO skuCostDTO = skuCostDTOS.stream().filter(e -> e.getSkuId().equals(productCostShowDTO.getSkuId())).findFirst().orElse(null);
                if (Objects.nonNull(skuCostDTO)){
                    BigDecimal rate = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO.getCurrency());
                    actualNoTaxCost =  MathUtil.multiply(rate,skuCostDTO.getProductCost(),4);
                    BigDecimal taxRate = productCostShowDTO.getTaxRate();
                    BigDecimal percentRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
                    actualTaxCost = MathUtil.multiply(actualNoTaxCost, MathUtil.add(BigDecimal.valueOf(1), percentRate),4);
                }
                //含税单价
                productCostShowDTO.setActualTaxCost(actualTaxCost);
                //不含税单价
                productCostShowDTO.setActualNoTaxCost(actualNoTaxCost);

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
            List<String> skuNoList = productCostShowDTOList.stream().map(ProductCostShowDTO::getSkuNo).collect(Collectors.toList());
            List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
            for (ProductCostShowDTO productCostShowDTO : productCostShowDTOList) {
                //成本信息
                DmpSkuCostEntity dmpSkuCostEntity = dmpSkuCostList.stream().filter(obj -> obj.getSkuNo().equals(productCostShowDTO.getSkuNo())).findFirst().orElse(null);
                BigDecimal actualTaxCost = BigDecimal.ZERO;
                BigDecimal actualNoTaxCost = BigDecimal.ZERO;
                if (ObjectUtil.isNotEmpty(dmpSkuCostEntity)) {
                    actualTaxCost = dmpSkuCostEntity.getCostPrice();
                    actualNoTaxCost = dmpSkuCostEntity.getNotTaxCostPrice();
                }
                //含税单价
                productCostShowDTO.setActualTaxCost(actualTaxCost);
                //不含税单价
                productCostShowDTO.setActualNoTaxCost(actualNoTaxCost);
            }
        }
        return productCostShowDTOList;
    }

    @Override
    public List<ProductCostEntity> listBySkuIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)){
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(ProductCostEntity::getSkuId, skuIds).list();
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




