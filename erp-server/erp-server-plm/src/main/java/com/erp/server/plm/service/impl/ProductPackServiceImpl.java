package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.model.plm.dto.ProductPackShowDTO;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.mapper.ProductPackMapper;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductPackService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(productPackDTO.getId())) {
                packEntity.setCreateUserId(loginUser.getUid());
                packEntity.setCreateUserName(loginUser.getUserName());
            } else {
                packEntity.setUpdateUserId(loginUser.getUid());
                packEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
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
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @param skuId 产品sku明细表id
     * @return java.lang.Boolean
     * @Description 删除产品包装信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     **/
    @Override
    public Boolean removePack(String skuId) {
        LambdaQueryWrapper<ProductPackEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductPackEntity::getSkuId, skuId);
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
        List<ProductPackEntity> list = this.lambdaQuery().in(ProductPackEntity::getSkuId, skuIds).list();
        //产品详情信息
        List<SkuVO> productDetailList = productDetailService.getSkuInfoBySkuIds(skuIds);
        List<ProductVO.ProductPackVO> resultList = new ArrayList<>(list.size());
        for (ProductPackEntity item : list) {
            String skuId = item.getSkuId();
            ProductVO.ProductPackVO packVO = new ProductVO.ProductPackVO();
            //产品大小
            String productSize = item.getProductSize();
            if (StringUtils.isNotBlank(productSize)) {
                String[] productSizes = productSize.split("X");
                //长
                packVO.setProductLength(new BigDecimal(productSizes[0]));
                //宽
                packVO.setProductWidth(new BigDecimal(productSizes[1]));
                //高
                packVO.setProductHeight(new BigDecimal(productSizes[2]));
            }

            //外箱大小
            String boxSize = item.getBoxSize();
            if (StringUtils.isNotBlank(productSize)) {
                String[] boxSizes = boxSize.split("X");
                //长
                packVO.setBoxLength(new BigDecimal(boxSizes[0]));
                //宽
                packVO.setBoxWidth(new BigDecimal(boxSizes[1]));
                //高
                packVO.setBoxHeight(new BigDecimal(boxSizes[2]));
            }
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
                packVO.setProductName(detail.getSpuName());
                packVO.setVariantProperty(detail.getVariantProperty());
                String skuImagesUrl = detail.getSkuImagesUrl();
                if(StringUtils.isNotBlank(skuImagesUrl)){
                    packVO.setSkuImageUrlList(Arrays.asList(skuImagesUrl.split(",")));
                }
            }
            packVO.setSkuId(skuId);
            resultList.add(packVO);

        }


        return resultList;
    }
}




