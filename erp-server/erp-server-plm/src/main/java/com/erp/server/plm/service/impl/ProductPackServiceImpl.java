package com.erp.server.plm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.LengthConverterUtil;
import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.model.plm.dto.ProductPackShowDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.entity.SysLogEntity;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.mapper.ProductPackMapper;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductPackService;
import com.erp.server.plm.service.SysLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Description 产品包装信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 15:35
 **/
@Service
public class ProductPackServiceImpl extends ServiceImpl<ProductPackMapper, ProductPackEntity>
        implements ProductPackService {

    @Resource
    private ProductPackMapper productPackMapper;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private SysLogService sysLogService;
    /**
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     * @Description 产品包装信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     **/
    @Override
    public List<ProductPackShowDTO> list(String productId) {
        return productPackMapper.list(productId);
    }

    /**
     * @param skuId
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     * @Description 产品包装信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     **/
    @Override
    public List<ProductPackShowDTO> listBySkuId(String skuId) {
        return productPackMapper.listBySkuId(skuId);
    }

    /**
     * @param productPackDTO 产品包装信息表
     * @return java.lang.Boolean
     * @Description 保存/修改产品包装信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     **/
    @Override
    public Boolean saveOrUpdate(ProductPackDTO productPackDTO) {
        ProductPackEntity packEntity = new ProductPackEntity();
        BeanMapper.copy(productPackDTO, packEntity);
        packEntity.handleData();
        return this.saveOrUpdate(packEntity);
    }

    /**
     * @param productPackList 产品包装信息表
     * @return java.lang.Boolean
     * @Description 保存/修改产品包装信息-批量操作
     * @Author Luo_WG
     * @Date 2022/9/26 18:16
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductPackDTO> productPackList) {
        List<ProductPackEntity> list = BeanMapper.copyList(productPackList, ProductPackEntity.class);
        list.forEach(ProductPackEntity::handleData);
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @param skuIds 产品sku明细表id
     * @return java.lang.Boolean
     * @Description 删除产品包装信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     **/
    @Override
    public Boolean removePack(List<String> skuIds) {
        LambdaQueryWrapper<ProductPackEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductPackEntity::getSkuId, skuIds);
        return this.remove(queryWrapper);
    }

    @Override
    public ProductPackEntity getBySkuId(String skuId) {
        LambdaQueryWrapper<ProductPackEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductPackEntity::getSkuId, skuId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 根据sku id 集合 获取到产品包装信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.ProductVO.ProductPackVO>
     * @author yl
     * @date 2023-04-17 17:43
     */
    @Override
    public List<ProductVO.ProductPackVO> getBySkuIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        //报关属性
        List<BasicDictEntity> dictList = basicDictService.listByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());
        List<ProductPackEntity> list = this.lambdaQuery().in(ProductPackEntity::getSkuId, skuIds).list();
        //产品详情信息
        List<SkuVO> productDetailList = productDetailService.listSkuPackByIds(skuIds);

        List<ProductVO.ProductPackVO> resultList = new ArrayList<>(list.size());
        for (ProductPackEntity item : list) {
            String skuId = item.getSkuId();
            ProductVO.ProductPackVO packVO = new ProductVO.ProductPackVO();
            //产品毛重
            packVO.setProductGrossWeight(item.getGrossWeight());
            packVO.setBoxQty(item.getBoxQty());
            //长
            packVO.setProductLength(LengthConverterUtil.mmToCm(item.getProductLength()));
            //宽
            packVO.setProductWidth(LengthConverterUtil.mmToCm(item.getProductWidth()));
            //高
            packVO.setProductHeight(LengthConverterUtil.mmToCm(item.getProductHeight()));
            //长
            packVO.setBoxLength(LengthConverterUtil.mmToCm(item.getBoxLength()));
            //宽
            packVO.setBoxWidth(LengthConverterUtil.mmToCm(item.getBoxWidth()));
            //高
            packVO.setBoxHeight(LengthConverterUtil.mmToCm(item.getBoxHeight()));
            //外箱重量
            BigDecimal boxWeight = item.getBoxWeight();
            if (boxWeight != null) {
                packVO.setBoxWeight(boxWeight);
            }
            //产品重量
            BigDecimal netWeight = item.getNetWeight();
            if (netWeight != null) {
                packVO.setProductNetWeight(netWeight);
            }
            SkuVO detail = productDetailList.stream().filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (detail != null) {
                packVO.setProductName(detail.getSkuName());
                packVO.setVariantProperty(detail.getVariantProperty());
                String skuImagesUrl = detail.getSkuImagesUrl();
                if (StringUtils.isNotBlank(skuImagesUrl)) {
                    packVO.setSkuImageUrlList(Arrays.asList(skuImagesUrl.split(",")));
                }
                packVO.setMaterials(detail.getMaterials());
                //产品属性
                String ProductProperty = dictList.stream().filter(d -> d.getId().equals(detail.getProductPropertyId())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse("");
                packVO.setLogisticsProductProperty(ProductProperty);
                packVO.setFunctionDesc(detail.getFunctionDesc());
                packVO.setProductGrade(detail.getProductGrade());
                packVO.setSkuNo(detail.getSkuNo());
            }
            packVO.setSkuId(skuId);
            resultList.add(packVO);

        }


        return resultList;
    }

    @Override
    public List<ProductPackEntity> findBySkuIds(List<String> skuIds) {
        return this.lambdaQuery().in(ProductPackEntity::getSkuId, skuIds).list();
    }

    @Override
    public void backFillPackaging(List<ProductPackDTO> productPackList) {
        if (CollectionUtils.isEmpty(productPackList)) {
            return;
        }
        for (ProductPackDTO productPackDTO : productPackList) {
            //更新尺寸信息
            this.updateProductPackPackaging(productPackDTO);
        }
    }

    @Override
    public List<ProductPackEntity> listBySkuIdList(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        return  lambdaQuery().in(ProductPackEntity::getSkuId,skuIdList).list();
    }

    /**
     * @description: 更新尺寸信息
     * @author Will
     * @date: 2023/10/11 10:02
     * @param productPackDTO
     */
    private void updateProductPackPackaging (ProductPackDTO productPackDTO) {
        lambdaUpdate()
		        .eq(ProductPackEntity::getSkuId,productPackDTO.getSkuId())
		        .set(productPackDTO.getProductLength() != null , ProductPackEntity::getProductLength,productPackDTO.getProductLength())
		        .set(productPackDTO.getProductWidth() != null , ProductPackEntity::getProductWidth,productPackDTO.getProductWidth())
		        .set(productPackDTO.getProductHeight() != null , ProductPackEntity::getProductHeight,productPackDTO.getProductHeight())
		        .set(productPackDTO.getBoxLength() != null , ProductPackEntity::getBoxLength,productPackDTO.getBoxLength())
		        .set(productPackDTO.getBoxWidth() != null , ProductPackEntity::getBoxWidth,productPackDTO.getBoxWidth())
		        .set(productPackDTO.getBoxHeight() != null , ProductPackEntity::getBoxHeight,productPackDTO.getBoxHeight())
		        .set(productPackDTO.getBoxQty() != null , ProductPackEntity::getBoxQty,productPackDTO.getBoxQty())
		        .set(productPackDTO.getBoxWeight() != null , ProductPackEntity::getBoxWeight,productPackDTO.getBoxWeight())
		        .set(productPackDTO.getNetWeight() != null , ProductPackEntity::getNetWeight,productPackDTO.getNetWeight())
                .update();
        StringBuilder logContentBuilder = new StringBuilder("更新了产品包装信息: ");

        if (productPackDTO.getProductLength() != null) {
            logContentBuilder.append("产品长度更新为").append(productPackDTO.getProductLength()).append("; ");
        }

        if (productPackDTO.getProductWidth() != null) {
            logContentBuilder.append("产品宽度更新为").append(productPackDTO.getProductWidth()).append("; ");
        }

        if (productPackDTO.getProductHeight() != null) {
            logContentBuilder.append("产品高度更新为").append(productPackDTO.getProductHeight()).append("; ");
        }

        if (productPackDTO.getBoxLength() != null) {
            logContentBuilder.append("箱子长度更新为").append(productPackDTO.getBoxLength()).append("; ");
        }

        if (productPackDTO.getBoxWidth() != null) {
            logContentBuilder.append("箱子宽度更新为").append(productPackDTO.getBoxWidth()).append("; ");
        }

        if (productPackDTO.getBoxHeight() != null) {
            logContentBuilder.append("箱子高度更新为").append(productPackDTO.getBoxHeight()).append("; ");
        }

        if (productPackDTO.getBoxQty() != null) {
            logContentBuilder.append("箱子数量更新为").append(productPackDTO.getBoxQty()).append("; ");
        }

        if (productPackDTO.getBoxWeight() != null) {
            logContentBuilder.append("箱子重量更新为").append(productPackDTO.getBoxWeight()).append("; ");
        }

        if (productPackDTO.getNetWeight() != null) {
            logContentBuilder.append("净重更新为").append(productPackDTO.getNetWeight()).append("; ");
        }

        String logContent = logContentBuilder.toString();

        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(String.valueOf(ProductDetailEntity.class))
                .setBusinessId(productPackDTO.getSkuId()).setOperation("QC质检").setContent(logContent));
    }
}




