package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.dto.OtherHistorySaleQtyDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.entity.SalesInfoEntity;
import com.erp.model.mrp.enums.FbaOrderTypeEnum;
import com.erp.model.mrp.enums.OverseasOrderTypeEnum;
import com.erp.model.mrp.enums.PlatformMappingTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.erp.server.mrp.mapper.SalesInfoMapper;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import com.erp.server.mrp.service.SalesInfoService;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 历史销量信息 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class SalesInfoServiceImpl extends SuperServiceImpl<SalesInfoMapper, SalesInfoEntity> implements SalesInfoService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OrderHistorySalesEsService orderHistorySalesEsService;

    @Resource
    @Lazy
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private CfgPlatformMappingService cfgPlatformMappingService;


    @Override
    public List<SalesInfoEntity> listByReplenishmentDetailIds(List<String> ids, LocalDate startDate, LocalDate endDate) {
        return list(Wrappers.<SalesInfoEntity>lambdaQuery()
                .in(SalesInfoEntity::getReplenishmentDetailId, ids)
                .between(SalesInfoEntity::getDate, startDate, endDate)
                .orderByAsc(SalesInfoEntity::getDate)
        );
    }

    @Override
    public List<SalesInfoEntity> listHistorySalesInfo(List<String> detailIdList) {
        return baseMapper.listHistorySalesInfo(detailIdList);
    }

    @Override
    public List<SalesInfoEntity> listByReplenishmentDetailIds(List<String> ids) {
        return list(Wrappers.<SalesInfoEntity>lambdaQuery()
                .in(SalesInfoEntity::getReplenishmentDetailId, ids)
                .orderByAsc(SalesInfoEntity::getDate)
        );
    }

    @Override
    public void dealHistorySaleQty() {
        List<OtherHistorySaleQtyDTO> otherHistorySaleQtyList = baseMapper.listHistorySaleQty();
        List<String> skuNoList = otherHistorySaleQtyList.stream()
                .map(OtherHistorySaleQtyDTO::getSkuNo)
                .distinct()
                .collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(skuNoList);
        Map<String, String> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuNo, SkuVO::getSkuId, (o1, o2) -> o1));
        List<ReplenishmentSuggestionEntity> list = replenishmentSuggestionService.list();
        Map<String, String> suggestionMap = list.stream().collect(Collectors.toMap(v -> v.getSkuId() + "-" + v.getShopId(), ReplenishmentSuggestionEntity::getId, (o1, o2) -> o1));
        List<CfgPlatformMappingEntity> mappings = cfgPlatformMappingService.listByEffective();
        Map<String, String> stringMap = mappings.stream()
                .collect(Collectors.toMap(CfgPlatformMappingEntity::getPlatform, CfgPlatformMappingEntity::getType, (o1,o2) -> o1));
        // 清除历史数据
        orderHistorySalesEsService.deleteByDateBetween(LocalDate.of(2023, 12, 1), LocalDate.of(2024, 8, 31));
        List<List<OtherHistorySaleQtyDTO>> partition = Lists.partition(otherHistorySaleQtyList, 1000);
        for (List<OtherHistorySaleQtyDTO> dtos : partition) {
            List<OrderHistorySalesEsEntity> historySales = new ArrayList<>();
            for (OtherHistorySaleQtyDTO dto : dtos) {
                String skuId = skuMap.get(dto.getSkuNo());
                if (ObjectUtils.isEmpty(skuId)) {
                    continue;
                }
                OrderHistorySalesEsEntity entity = new OrderHistorySalesEsEntity();
                entity.setOriginalSalesQty(dto.getQty());
                entity.setShopId(dto.getShopId());
                entity.setSkuId(skuId);
                entity.setDate(dto.getOrderDate());
                String type = stringMap.get(dto.getPlatform());
                if (PlatformMappingTypeEnum.AMAZON_PLATFORM.getCode().equals(type)) {
                    entity.setOrderType(FbaOrderTypeEnum.FBA.getCode());
                } else {
                    entity.setOrderType(OverseasOrderTypeEnum.OVERSEAS_WAREHOUSE.getCode());
                }
                entity.setReplenishmentId(suggestionMap.get(skuId + "-" + dto.getShopId()));
                historySales.add(entity);
            }
            orderHistorySalesEsService.saveAll(historySales);
        }
    }

}
