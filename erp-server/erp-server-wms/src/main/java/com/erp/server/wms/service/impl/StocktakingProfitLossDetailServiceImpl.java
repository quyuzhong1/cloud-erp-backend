package com.erp.server.wms.service.impl;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.entity.StocktakingProfitLossDetailEntity;
import com.erp.server.wms.mapper.StocktakingProfitLossDetailMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.StocktakingProfitLossDetailService;
import com.common.business.service.SuperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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
            ProductDetailEntity sku=skuList.stream().filter(s->s.getId().equals(skuId)).findFirst().orElse(null);
            if(Objects.nonNull(sku)){
                item.setProductName(sku.getName());
                item.setUnit(sku.getUnitName());
            }else{
                item.setProductName("");
                item.setUnit("");
            }
        }
        return viewList;
    }
}
