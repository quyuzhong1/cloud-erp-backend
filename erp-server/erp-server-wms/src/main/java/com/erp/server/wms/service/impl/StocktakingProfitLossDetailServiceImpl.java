package com.erp.server.wms.service.impl;

import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.StocktakingProfitLossDetailEntity;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.erp.server.wms.mapper.StocktakingProfitLossDetailMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.StocktakingProfitLossDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘盈盘亏单详情 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Slf4j
@Service
public class StocktakingProfitLossDetailServiceImpl extends SuperServiceImpl<StocktakingProfitLossDetailMapper, StocktakingProfitLossDetailEntity> implements StocktakingProfitLossDetailService {

    @Resource
    private ProductDetailService productDetailService;

    /**
     * 根据主表id 获取到对应详情信息
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.dto.StocktakingProfitLossDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-08-11 10:11
     */
    @Override
    public List<StocktakingProfitLossDetailDTO.ViewDTO> listByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        List<StocktakingProfitLossDetailDTO.ViewDTO> viewList = baseMapper.listByMainIds(mainIdList);
        List<String> skuIdList = viewList.stream().map(StocktakingProfitLossDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listProductDetailByIds(skuIdList);
        for (StocktakingProfitLossDetailDTO.ViewDTO item : viewList) {
            String skuId = item.getSkuId();
            ProductDetailEntity sku = skuList.stream().filter(s -> s.getId().equals(skuId)).findFirst().orElse(null);
            if (Objects.nonNull(sku)) {
                item.setProductName(sku.getName());
                item.setUnit(sku.getUnitName());
            } else {
                item.setProductName("");
                item.setUnit("");
            }
        }
        return viewList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateInfo(String mainId, List<StocktakingProfitLossDetailDTO.UpdateDTO> detailList) {
        List<StocktakingProfitLossDetailEntity> dbList = this.listBaseByMainId(mainId);
        List<Pair<String, String>> pairList = detailList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<StocktakingProfitLossDetailEntity> updateList = BeanMapperUtils.copyList(StocktakingProfitLossDetailEntity.class, detailList);
        updateList.forEach(u -> u.setMainId(mainId));
        this.saveOrUpdateBatch(updateList);
    }

    @Override
    public void removeByMainId(String mainId) {
        lambdaUpdate().eq(StocktakingProfitLossDetailEntity::getMainId, mainId).remove();
    }

    /**
     * 获取删除ids
     *
     * @param pairList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-10-20 14:05
     */
    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<StocktakingProfitLossDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(StocktakingProfitLossDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<StocktakingProfitLossDetailEntity> listBaseByMainId(String mainId) {
        if (StringUtils.isNotBlank(mainId)) {
            return this.lambdaQuery().eq(StocktakingProfitLossDetailEntity::getMainId, mainId).orderByAsc(StocktakingProfitLossDetailEntity::getId).list();
        }
        return Collections.emptyList();
    }
}
