package com.erp.server.bi.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.dto.BiCategoryDTO;
import com.erp.model.bi.dto.SkuSalesDTO;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.entity.BiProductInfoEntity;
import com.erp.model.bi.vo.SkuCategoryVO;
import com.erp.model.bi.vo.SkuDetailVO;
import com.erp.server.bi.mapper.BiProductDetailMapper;
import com.erp.server.bi.service.BiProductDetailService;
import com.erp.server.bi.service.BiProductInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品sku表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-21
 */
@Service
public class BiProductDetailServiceImpl extends SuperServiceImpl<BiProductDetailMapper, BiProductDetailEntity> implements BiProductDetailService {

    @Resource
    private BiProductInfoService biProductInfoService;


    /**
     * 获取分类一级下面的sku 信息
     *
     * @param
     * @return java.util.List<com.erp.model.bi.vo.SkuCategoryVO>
     * @author yl
     * @date 2023-04-21 10:20
     */
    @Override
    public List<SkuCategoryVO> getSkuCategoryList(List<String> categoryIdList) {
        List<BiCategoryDTO.ProductCategoryDTO> productCategoryList = baseMapper.listByCategoryIds(categoryIdList);
        Map<String, List<BiCategoryDTO.ProductCategoryDTO>> productCategoryMap = productCategoryList.parallelStream().
                collect(Collectors.groupingBy(BiCategoryDTO.ProductCategoryDTO::getCategoryId));
        List<SkuCategoryVO> resultList = new ArrayList<>(productCategoryMap.size());
        for (Map.Entry<String, List<BiCategoryDTO.ProductCategoryDTO>> item : productCategoryMap.entrySet()) {
            SkuCategoryVO vo = new SkuCategoryVO();
            List<BiCategoryDTO.ProductCategoryDTO> valueList = item.getValue();
            List<String> skuNoList = valueList.stream().map(BiCategoryDTO.ProductCategoryDTO::getSkuNo).collect(Collectors.toList());
            vo.setCategoryId(item.getKey());
            vo.setSkuList(skuNoList);
            vo.setName(valueList.get(0).getProductName());
            resultList.add(vo);
        }
        return resultList;
    }


    /**
     * 获取品牌下面的sku 信息
     *
     * @param
     * @return java.util.List<com.erp.model.bi.vo.SkuCategoryVO>
     * @author yl
     * @date 2023-04-21 10:20
     */
    @Override
    public List<SkuCategoryVO> getSkuBrandList(List<String> brandList) {
        List<BiProductInfoEntity> productList = biProductInfoService.getbrandList(brandList);

        Map<String, List<BiProductInfoEntity>> productMap = productList.parallelStream().
                collect(Collectors.groupingBy(BiProductInfoEntity::getBrandName));
        List<SkuCategoryVO> resultList = new ArrayList<>(productMap.size());
        List<BiProductDetailEntity> detailList = this.list();
        for (Map.Entry<String, List<BiProductInfoEntity>> item : productMap.entrySet()) {
            SkuCategoryVO vo = new SkuCategoryVO();
            List<BiProductInfoEntity> productInfoList = item.getValue();
            List<String> productIds = productInfoList.stream().map(BiProductInfoEntity::getId).collect(Collectors.toList());
            vo.setName(item.getKey());
            List<String> skuNoList = detailList.stream().filter(d -> productIds.contains(d.getProductId())).map(BiProductDetailEntity::getSkuNo).collect(Collectors.toList());
            vo.setSkuList(skuNoList);
            resultList.add(vo);
        }
        return resultList;
    }


    /**
     * 根据属性 获取到对应的sku 信息
     *
     * @param
     * @return java.util.List<com.erp.model.bi.vo.SkuCategoryVO>
     * @author yl
     * @date 2023-04-21 10:40
     */
    @Override
    public List<SkuCategoryVO> getSkuPropertyList() {
        List<BiProductInfoEntity> productList = biProductInfoService.list();
        Map<String, List<BiProductInfoEntity>> productMap = productList.parallelStream().
                collect(Collectors.groupingBy(BiProductInfoEntity::getProperty));
        List<SkuCategoryVO> resultList = new ArrayList<>(productMap.size());
        List<BiProductDetailEntity> detailList = this.list();
        for (Map.Entry<String, List<BiProductInfoEntity>> item : productMap.entrySet()) {
            SkuCategoryVO vo = new SkuCategoryVO();
            List<BiProductInfoEntity> productInfoList = item.getValue();
            List<String> productIds = productInfoList.stream().map(BiProductInfoEntity::getId).collect(Collectors.toList());
            vo.setName(item.getKey());
            List<String> skuNoList = detailList.stream().filter(d -> productIds.contains(d.getProductId())).map(BiProductDetailEntity::getSkuNo).collect(Collectors.toList());
            vo.setSkuList(skuNoList);
            resultList.add(vo);
        }
        return resultList;
    }

    /**
     * 根据sku 获取详情信息
     *
     * @param skuNo
     * @return
     */
    @Override
    public BiProductDetailEntity getBySkuNo(String skuNo) {
        if (StringUtils.isBlank(skuNo)) {
            return null;
        }
        return this.lambdaQuery().eq(BiProductDetailEntity::getSkuNo, skuNo).last("LIMIT 1").one();
    }

    /**
     * 根据sku no list 获取信息
     *
     * @param skuNoList
     * @return
     */
    @Override
    public List<BiProductDetailEntity> listBySkuNoList(List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(BiProductDetailEntity::getSkuNo, skuNoList).list();
    }


    @Override
    public List<SkuDetailVO> getSkuIdBySkuNo(List<String> skuNos) {
        if (CollectionUtils.isEmpty(skuNos)) {
            return Collections.emptyList();
        }
        return baseMapper.getSkuIdBySkuNo(skuNos);
    }


    /**
     * 获取到sku信息
     *
     * @param skuIdList
     * @return java.util.List<com.erp.model.bi.dto.SkuSalesDTO.ProductSkuDTO>
     * @author yl
     * @date 2023-09-26 18:51
     */
    @Override
    public List<SkuSalesDTO.ProductSkuDTO> listProductSkuBySkuIdList(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listProductSkuBySkuIdList(skuIdList);
    }
}
