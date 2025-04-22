package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.AliexpressDeliveryDetailMapper;
import com.erp.server.wms.service.AliexpressDeliveryDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 速卖通发货单详情 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-05-06
 */
@Slf4j
@Service
public class AliexpressDeliveryDetailServiceImpl extends SuperServiceImpl<AliexpressDeliveryDetailMapper, AliexpressDeliveryDetailEntity> implements AliexpressDeliveryDetailService {


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean addOrUpdate(List<AliexpressDeliveryDetailDTO.AddDTO> addDTO, String platformCode) {
        if(CollectionUtils.isEmpty(addDTO)){
            return true;
        }
        if (addDTO.stream().anyMatch(e-> StringUtils.isBlank(e.getUniqueId()))){
            ServiceException.runError("速卖通发货明细唯一ID为空,平台单号【{}】", platformCode);
        }
        if (addDTO.stream().anyMatch(e-> StringUtils.isBlank(e.getPlatformSpuNo()))){
            ServiceException.runError("速卖通发货明细产品ID为空,平台单号【{}】", platformCode);
        }

        //先删除后新增
//        this.removeByMainId(addDTO.get(0).getMainId());
        List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList = BeanUtil.copyToList(addDTO,AliexpressDeliveryDetailEntity.class);

        // 补充明细ID
        aliexpressDeliveryDetailEntityList = fillData(addDTO, aliexpressDeliveryDetailEntityList, platformCode);

        log.info("开始新增/更新速卖通发货单详情");
        boolean save = super.saveOrUpdateBatch(aliexpressDeliveryDetailEntityList);
        if(!save) {
            throw new ServiceException("速卖通发货单详情新增/更新失败");
        }
        return true;
    }

    /**
     * 补充信息
     */
    private List<AliexpressDeliveryDetailEntity> fillData(List<AliexpressDeliveryDetailDTO.AddDTO> addDTO, List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList, String platformCode) {
        String mainId = addDTO.get(0).getMainId();
        // 查询历史已存在明细
        List<String> detailUniqueIds = addDTO.stream().map(AliexpressDeliveryDetailDTO.AddDTO::getUniqueId).distinct().collect(Collectors.toList());
        Map<String, AliexpressDeliveryDetailEntity> existDetailMap = lambdaQuery()
                .eq(AliexpressDeliveryDetailEntity::getMainId, mainId)
                .in(AliexpressDeliveryDetailEntity::getUniqueId, detailUniqueIds)
                .list()
                .stream()
                .collect(Collectors.toMap(AliexpressDeliveryDetailEntity::getUniqueId, e -> e));
        if (!existDetailMap.isEmpty()) {
            // 根据唯一ID设置已存在ID
            for (AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity : aliexpressDeliveryDetailEntityList) {
                AliexpressDeliveryDetailEntity existDetailEntity = existDetailMap.get(aliexpressDeliveryDetailEntity.getUniqueId());
                if(null != existDetailEntity){
                    aliexpressDeliveryDetailEntity.setId(existDetailEntity.getId());
                    aliexpressDeliveryDetailEntity.setCreateTime(existDetailEntity.getCreateTime());
                }
            }
        }
        // 补充发货价格信息和来源明细ID
        // 查询主单
        List<SoB2cEntity> soB2cEntityList = FeignQuery.create(SoB2cEntity.class)
                .eq(SoB2cEntity::getPlatformCode, platformCode)
                .list();
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            ServiceException.runError("【速卖通发货生成】:未找到B2C销售订单：平台单号【{}】", platformCode);
        }
        List<String> soIds = soB2cEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = FeignQuery.create(SoB2cDetailEntity.class)
                .in(SoB2cDetailEntity::getMainId, soIds)
                .list();
        if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
            ServiceException.runError("【速卖通发货生成】:未找到B2C销售订单明细：平台单号【{}】", platformCode);
        }
        //产品信息
        List<String> skuNos = aliexpressDeliveryDetailEntityList.stream().map(AliexpressDeliveryDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        skuNos.addAll(soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuNo).distinct().collect(Collectors.toList()));
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        // 重新分摊价格
        return convertAllAliExpressDeliveryDetailPrice(aliexpressDeliveryDetailEntityList, soB2cDetailEntityList, skuVOList);
    }


    public boolean removeByMainId(String mainId){
        return this.lambdaUpdate().eq(AliexpressDeliveryDetailEntity::getMainId, mainId).remove();
    }

    /**
     * 计算自发发货单明细单价
     *
     * @param deliveryDetailList    速卖通发货单明细
     * @param soB2cDetailEntityList 速卖通销售订单明细
     * @param skuVOList             sku列表
     * @return Map<速卖通明细ID, Pair<发货明细sku单价, 发货明细sku总价>
     */
    public List<AliexpressDeliveryDetailEntity> convertAllAliExpressDeliveryDetailPrice(List<AliexpressDeliveryDetailEntity> deliveryDetailList,
                                                                                             List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                                                             List<SkuVO> skuVOList
    ) {
        List<AliexpressDeliveryDetailEntity> deliveryDetailResultList = new LinkedList<>();

        Map<String, SkuVO> skuVoMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, e -> e));

        // 平台产品ID 分组
        Map<String, List<AliexpressDeliveryDetailEntity>> deliveryMap = deliveryDetailList
                .stream()
                .collect(Collectors.groupingBy(AliexpressDeliveryDetailEntity::getPlatformSkuId));

        for (Map.Entry<String, List<AliexpressDeliveryDetailEntity>> entry : deliveryMap.entrySet()) {
            // 未拆分
            if (1 == entry.getValue().size()){
                for (AliexpressDeliveryDetailEntity aliExpressDetailEntity : entry.getValue()) {
                    SoB2cDetailEntity detailEntity = checkAndGetSoB2cDetailEntity(soB2cDetailEntityList, entry);
                    // 根据发货数量和订单明细数量判断单价和发货明细总价
                    // Map<速卖通明细ID, Pair<发货明细单价, 发货明细总价>>
                    Pair<BigDecimal, BigDecimal> pircePair = checkQtyGetPrice(aliExpressDetailEntity, detailEntity);
                    aliExpressDetailEntity.setPlatformDetailId(detailEntity.getSourceDetailId());
                    aliExpressDetailEntity.setProratedUnitPrice(pircePair.getFirst());
                    aliExpressDetailEntity.setProratedAmount(pircePair.getSecond());
                    aliExpressDetailEntity.setBomQty(aliExpressDetailEntity.getOrderLineQty() / detailEntity.getQty());
                    BigDecimal costPrice = getCostPrice(skuVOList, aliExpressDetailEntity);
                    aliExpressDetailEntity.setCostPrice(costPrice);
                    deliveryDetailResultList.add(aliExpressDetailEntity);
                }
                continue;
            }
            // 平台库存产品ID一样 = 未拆分
            if (1 == entry.getValue().stream().map(AliexpressDeliveryDetailEntity::getScItemId).count()){
                for (AliexpressDeliveryDetailEntity aliExpressDetailEntity : entry.getValue()) {
                    SoB2cDetailEntity detailEntity = checkAndGetSoB2cDetailEntity(soB2cDetailEntityList, entry);
                    // 根据发货数量和订单明细数量判断单价和发货明细总价
                    aliExpressDetailEntity.setPlatformDetailId(detailEntity.getSourceDetailId());
                    aliExpressDetailEntity.setProratedUnitPrice(aliExpressDetailEntity.getPrice());
                    aliExpressDetailEntity.setProratedAmount(detailEntity.getAmount());
                    aliExpressDetailEntity.setBomQty(aliExpressDetailEntity.getOrderLineQty() / detailEntity.getQty());
                    BigDecimal costPrice = getCostPrice(skuVOList, aliExpressDetailEntity);
                    aliExpressDetailEntity.setCostPrice(costPrice);
                    deliveryDetailResultList.add(aliExpressDetailEntity);
                }
                continue;
            }

            // 总单价
            BigDecimal price = entry.getValue().get(0).getPrice();

            // 根据产品ID匹配, 目前速卖通明细产品ID唯一
            SoB2cDetailEntity detailEntity = checkAndGetSoB2cDetailEntity(soB2cDetailEntityList, entry);

            BigDecimal totalCostAmount = BigDecimal.ZERO;
            // 计算总成本
            // sku
            for (AliexpressDeliveryDetailEntity deliveryDetailEntity : deliveryDetailList) {
                SkuVO skuVO = skuVoMap.get(deliveryDetailEntity.getSkuId());
                if (null == skuVO){
                    ServiceException.runError("未找sku信息:skuId={}", deliveryDetailEntity.getSkuId());
                }
                BigDecimal costPrice = ObjectUtils.isEmpty(skuVO.getActualTaxCost()) ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost();
                if (null == costPrice){
                    ServiceException.runError("未找到成本信息:skuId={}", deliveryDetailEntity.getSkuId());
                }
                if (0 == costPrice.compareTo(BigDecimal.ZERO)){
                    ServiceException.runError("成本信息为0:skuId={}", deliveryDetailEntity.getSkuId());
                }
                BigDecimal allItemPrice = costPrice.multiply(BigDecimal.valueOf(deliveryDetailEntity.getOrderLineQty()));
                totalCostAmount = totalCostAmount.add(allItemPrice);
            }
            totalCostAmount = totalCostAmount.divide(BigDecimal.valueOf(detailEntity.getQty()), 4, RoundingMode.DOWN);

            // 汇总
            Map<String, List<AliexpressDeliveryDetailEntity>> groupMap = entry.getValue()
                    .stream()
                    .collect(Collectors.groupingBy(AliexpressDeliveryDetailEntity::getSkuId));
            List<Map.Entry<String, List<AliexpressDeliveryDetailEntity>>> entryList = groupMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());

            // 剩余价格
            BigDecimal lastPrice = price;
            // 平台明细总价
            BigDecimal lastTotalPrice = detailEntity.getAmount();
            for (int i = 0; i < entryList.size(); i++) {
                Map.Entry<String, List<AliexpressDeliveryDetailEntity>> curEntry = entryList.get(i);

                SkuVO skuVO = skuVoMap.get(curEntry.getKey());
                List<AliexpressDeliveryDetailEntity> value = curEntry.getValue();
                if (i == entryList.size() - 1){
                    // 判断当前ERP sku发货数量 是否和 明细数量
                    int sum = value.stream().mapToInt(AliexpressDeliveryDetailEntity::getOrderLineQty).sum();
                    BigDecimal targetLastPrice = lastPrice;
                    if (sum > detailEntity.getQty()){
                        targetLastPrice = lastPrice.multiply(BigDecimal.valueOf(detailEntity.getQty())).divide(BigDecimal.valueOf(sum), 4, RoundingMode.DOWN);
                    }
                    for (AliexpressDeliveryDetailEntity deliveryDetailEntity : value) {
                        deliveryDetailEntity.setPlatformDetailId(detailEntity.getSourceDetailId());
                        deliveryDetailEntity.setProratedUnitPrice(targetLastPrice);
                        deliveryDetailEntity.setProratedAmount(lastTotalPrice);
                        deliveryDetailEntity.setBomQty(deliveryDetailEntity.getOrderLineQty() / detailEntity.getQty());
                        deliveryDetailEntity.setCostPrice(skuVO.getActualTaxCost());
                        deliveryDetailResultList.add(deliveryDetailEntity);
                        lastTotalPrice = targetLastPrice.multiply(BigDecimal.valueOf(deliveryDetailEntity.getOrderLineQty()));
                    }
                } else {
                    // 当前单价 = 明细单价 * (成本 / 总成本)
                    BigDecimal curPrice = price.multiply(skuVO.getActualTaxCost())
                            .divide(totalCostAmount, 4, RoundingMode.DOWN);
                    for (AliexpressDeliveryDetailEntity deliveryDetailEntity : value) {
                        deliveryDetailEntity.setPlatformDetailId(detailEntity.getSourceDetailId());
                        deliveryDetailEntity.setProratedUnitPrice(curPrice);
                        deliveryDetailEntity.setProratedAmount(curPrice.multiply(BigDecimal.valueOf(deliveryDetailEntity.getOrderLineQty())));
                        deliveryDetailEntity.setBomQty(deliveryDetailEntity.getOrderLineQty() / detailEntity.getQty());
                        deliveryDetailEntity.setCostPrice(skuVO.getActualTaxCost());
                        deliveryDetailResultList.add(deliveryDetailEntity);
                    }
                    int sum = value.stream().mapToInt(AliexpressDeliveryDetailEntity::getOrderLineQty).sum();
                    // 剩余总价 = 单价 * 总sku数量
                    BigDecimal planBomTotalPrice = curPrice.multiply(BigDecimal.valueOf(sum));
                    // 剩余单价 = 当前单价 * 发货明细数量 / 明细数量
                    BigDecimal planBomPrice = planBomTotalPrice
                            .divide(BigDecimal.valueOf(detailEntity.getQty()), 4, RoundingMode.DOWN);
                    lastPrice = lastPrice.subtract(planBomPrice);
                    // 剩余总价 = 当前总价 -（当前sku总价）
                    lastTotalPrice = lastTotalPrice.subtract(planBomTotalPrice);
                }
            }
        }

        return deliveryDetailResultList;
    }

    private static BigDecimal getCostPrice(List<SkuVO> skuVOList, AliexpressDeliveryDetailEntity aliExpressDetailEntity) {
        SkuVO skuVO = skuVOList.stream().filter(e -> e.getSkuId().equalsIgnoreCase(aliExpressDetailEntity.getSkuId())).findFirst().orElse(null);
        BigDecimal costPrice = BigDecimal.ZERO;
        if (null != skuVO){
            costPrice = skuVO.getActualTaxCost();
        }
        return costPrice;
    }

    /**
     * 检查匹配订单明细
     */
    private static SoB2cDetailEntity checkAndGetSoB2cDetailEntity(List<SoB2cDetailEntity> soB2cDetailEntityList, Map.Entry<String, List<AliexpressDeliveryDetailEntity>> entry) {
        // 平台skuId 优先
        SoB2cDetailEntity detailEntity = soB2cDetailEntityList.stream()
                .filter(e -> e.getPlatformSkuId().equalsIgnoreCase(entry.getKey()))
                .findFirst()
                .orElse(null);
        if (null == detailEntity){
            // 平台产品ID匹配
            String platformSpuNo = entry.getValue().stream().map(AliexpressDeliveryDetailEntity::getPlatformSpuNo).findFirst().orElse("");
            detailEntity= soB2cDetailEntityList.stream()
                    .filter(e -> e.getPlatformSpuNo().equalsIgnoreCase(platformSpuNo))
                    .findFirst()
                    .orElse(null);
        }
        if (null == detailEntity){
           throw  new ServiceException("未找对应明细:产品sku ID={}", entry.getKey());
        }
        return detailEntity;
    }


    /**
     * 根据发货数量和订单明细数量判断单价
     * @param aliExpressDetailEntity 速卖通发货单明细
     * @param detailEntity 订单明细
     * @return 计算后发货sku单价
     */
    private Pair<BigDecimal, BigDecimal> checkQtyGetPrice(AliexpressDeliveryDetailEntity aliExpressDetailEntity, SoB2cDetailEntity detailEntity) {
        if (aliExpressDetailEntity.getOrderLineQty() > detailEntity.getQty()){
            // 速卖通发货单数量大于订单明细数量
            // 速卖通发货单价格 * 订单明细数量 / 速卖通发货单发货数量
            BigDecimal price = aliExpressDetailEntity.getPrice()
                    .multiply(BigDecimal.valueOf(detailEntity.getQty()))
                    .divide(BigDecimal.valueOf(aliExpressDetailEntity.getOrderLineQty()), 4, RoundingMode.DOWN);
            return new Pair<>(price, detailEntity.getAmount());
        } else {
            //  速卖通发货单数量小于等于订单明细数量
            return new Pair<>(aliExpressDetailEntity.getPrice(), detailEntity.getAmount());
        }
    }
}
