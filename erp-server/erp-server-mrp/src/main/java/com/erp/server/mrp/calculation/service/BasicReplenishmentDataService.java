package com.erp.server.mrp.calculation.service;

import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BasicReplenishmentDataService {

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private CfgPlatformMappingService cfgPlatformMappingService;
    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    public void initReplenishmentSku() {
        //获取所有已审核且存在上市时间得非费用服务类sku
        List<SkuVO> vos = plmTaskFeign.listApproveAndListingSku();
        //获取已生成补货基础数据得信息
        List<ReplenishmentSuggestionEntity> replenishmentSuggestion = replenishmentSuggestionService.listAllSkuAndShop();
        List<String> suggestionList = replenishmentSuggestion.stream().map(v -> v.getSkuId() + "|" + v.getSkuNo() + "|" + v.getShopId()).collect(Collectors.toList());
        //获取所有店铺
        ApiResult<List<ShopInfoEntity>> allShopResult = shopInfoFeign.list();
        if (!allShopResult.isSuccess()) {
            throw new ServiceException(ApiError.ERROR_1023);
        }
        List<ReplenishmentSuggestionEntity> suggestionLists = new ArrayList<>();
        List<CfgPlatformMappingEntity> mappings = cfgPlatformMappingService.listByEffective();
        for (CfgPlatformMappingEntity mapping : mappings) {
            for (ShopInfoEntity shopInfo : allShopResult.getData()) {
                ReplenishmentSuggestionEntity entity = new ReplenishmentSuggestionEntity();
                entity.setShopId(shopInfo.getId());
                entity.setCountry(shopInfo.getDictCountryCode());
                entity.setPlatformType(mapping.getType());
                entity.setPlatform(shopInfo.getDictPlatform());
            }
        }

        //根据映射表分类店铺数据
        Map<String, List<ShopInfoEntity>> shopMap = mappings.parallelStream()
                .collect(Collectors.toMap(CfgPlatformMappingEntity::getType, v -> allShopResult.getData().stream().filter(shop -> shop.getDictPlatform().equals(v.getPlatform())).collect(Collectors.toList()), (o1, o2) -> {
                    o1.addAll(o2);
                    return o1;
                }));
        shopMap.entrySet().stream().map(entry -> vos.parallelStream().map(e -> {
            if (suggestionList.contains(e.getSkuId() + "|" + e.getSkuNo() + "|" + entry.getValue())) {

            }
            ReplenishmentSuggestionEntity entity = new ReplenishmentSuggestionEntity();
            return entity;
        }));

    }

}
