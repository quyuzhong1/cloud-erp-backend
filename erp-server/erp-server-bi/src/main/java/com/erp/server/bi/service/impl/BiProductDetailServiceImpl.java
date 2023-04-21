package com.erp.server.bi.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.entity.BiProductInfoEntity;
import com.erp.model.bi.vo.SkuCategoryVO;
import com.erp.server.bi.mapper.BiProductDetailMapper;
import com.erp.server.bi.service.BiProductDetailService;
import com.erp.server.bi.service.BiProductInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    public List<SkuCategoryVO> getSkuCategoryList() {
        List<BiProductInfoEntity> productList = biProductInfoService.list();
        Map<String, List<BiProductInfoEntity>> productMap = productList.parallelStream().
                collect(Collectors.groupingBy(BiProductInfoEntity::getCategory));
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
     * 获取品牌下面的sku 信息
     * @author yl
     * @date 2023-04-21 10:20
     * @param
     * @return java.util.List<com.erp.model.bi.vo.SkuCategoryVO>
     */
    @Override
    public List<SkuCategoryVO> getSkuBrandList() {
        List<BiProductInfoEntity> productList = biProductInfoService.list();
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
     * @author yl
     * @date 2023-04-21 10:40
     * @param
     * @return java.util.List<com.erp.model.bi.vo.SkuCategoryVO>
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
}
